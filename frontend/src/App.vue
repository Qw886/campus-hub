<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { get, post, put, remove, uploadImage } from './api'

const view = ref('activities')
const showAuth = ref(false)
const authMode = ref('login')
const authError = ref('')
const notice = ref('')
const noticeType = ref('success')
const loading = ref(false)

const user = ref(readStored('campus_user'))
const detail = ref(null)
const detailSignupState = ref({ activityId: null, active: false, checkedIn: false })
const categories = ref([])
const activities = ref([])
const activityPage = ref({ records: [], total: 0, page: 1, size: 6 })
const mySignups = ref({ records: [], total: 0 })
const organizerActivities = ref({ records: [], total: 0 })
const adminActivities = ref({ records: [], total: 0 })
const participants = ref({ records: [], total: 0 })
const participantActivity = ref(null)
const editingActivityId = ref(null)
const editingAuditReason = ref('')
const organizerStatus = ref('')
const adminStatus = ref('1')
const mySignupPage = ref(1)
const organizerPage = ref(1)
const adminPage = ref(1)
const participantPage = ref(1)
const pageSize = 8
const uploading = ref(false)

const query = reactive({ keyword: '', categoryId: '', page: 1, size: 6 })
const authForm = reactive({ username: '', password: '', nickname: '' })
const profileForm = reactive({ nickname: '', phone: '', avatar: '' })
const profilePhoneError = computed(() => {
  const phone = profileForm.phone.trim()
  return phone && !/^1[3-9]\d{9}$/.test(phone) ? '请输入以 13–19 开头的 11 位大陆手机号，或留空' : ''
})
const organizerApplication = ref(null)
const organizerReason = ref('')
const pendingOrganizerApplications = ref({ records: [], total: 0 })
const createForm = reactive({
  title: '',
  description: '',
  coverUrl: '',
  location: '',
  categoryId: 1,
  maxParticipants: 30,
  registrationStartTime: toInputDateTime(new Date()),
  registrationEndTime: toInputDateTime(addMinutes(new Date(), 60)),
  startTime: toInputDateTime(addMinutes(new Date(), 120)),
  endTime: toInputDateTime(addMinutes(new Date(), 240))
})

const isLoggedIn = computed(() => Boolean(user.value))
const isStudent = computed(() => user.value?.role === 'USER')
const isOrganizer = computed(() => user.value?.role === 'ORGANIZER')
const isAdmin = computed(() => user.value?.role === 'ADMIN')
const totalPages = computed(() => Math.max(1, Math.ceil(Number(activityPage.value.total || 0) / query.size)))
const mySignupPages = computed(() => Math.max(1, Math.ceil(Number(mySignups.value.total || 0) / pageSize)))
const organizerPages = computed(() => Math.max(1, Math.ceil(Number(organizerActivities.value.total || 0) / pageSize)))
const adminPages = computed(() => Math.max(1, Math.ceil(Number(adminActivities.value.total || 0) / pageSize)))
const participantPages = computed(() => Math.max(1, Math.ceil(Number(participants.value.total || 0) / pageSize)))

function hasActiveSignup(activityId) {
  return detailSignupState.value.activityId === activityId && detailSignupState.value.active
}

function signupIssue(activity) {
  if (!activity) return ''
  const now = Date.now()
  if (now < new Date(activity.signupStartTime).getTime()) return '报名尚未开始'
  if (now >= new Date(activity.signupEndTime).getTime()) return '报名已截止'
  if (Number(activity.currentParticipants) >= Number(activity.maxParticipants)) return '名额已满'
  return ''
}

function canCancelSignup(activity) {
  return Boolean(activity?.signupEndTime && Date.now() < new Date(activity.signupEndTime).getTime())
}

function signupStatusLabel(item) {
  if (item.signupStatus === 2) return '已取消报名'
  if (item.checkedIn) return '已签到'
  if (item.activityStatus === 3) return '活动已结束 · 已报名'
  if (item.activityStatus === 4) return '活动已取消'
  if (item.activityStatus === 5) return '活动审核未通过'
  return '已报名'
}

function canCheckin(item) {
  if (!item || item.signupStatus !== 1 || item.activityStatus !== 2 || item.checkedIn || !item.activityStartTime) return false
  return Date.now() >= new Date(item.activityStartTime).getTime() - 30 * 60 * 1000
}

function readStored(key) {
  try {
    return JSON.parse(localStorage.getItem(key) || 'null')
  } catch {
    return null
  }
}

function addMinutes(date, minutes) {
  return new Date(date.getTime() + minutes * 60 * 1000)
}

function toInputDateTime(date) {
  const pad = (value) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function apiTimeToInput(value) {
  return value ? String(value).slice(0, 16) : ''
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { hour12: false })
}

function statusLabel(status) {
  return ({ 1: '待审核', 2: '报名中', 3: '已结束', 4: '已取消', 5: '审核拒绝' })[status] || '未知状态'
}

function statusClass(status) {
  return `status-${status}`
}

function categoryName(categoryId) {
  return categories.value.find(category => Number(category.id) === Number(categoryId))?.name || '其他'
}

function roleLabel(role) {
  return ({ USER: '学生', ORGANIZER: '组织者', ADMIN: '管理员' })[role] || role
}

function setNotice(message, type = 'success') {
  notice.value = message
  noticeType.value = type
  window.setTimeout(() => {
    if (notice.value === message) notice.value = ''
  }, 3600)
}

function scrollToActivities() {
  document.querySelector('.activity-section')?.scrollIntoView({ behavior: 'smooth' })
}

async function loadCategories() {
  try {
    categories.value = await get('/activity-categories') || []
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function loadActivities(page = query.page) {
  loading.value = true
  query.page = page
  const params = new URLSearchParams({ page: String(page), size: String(query.size) })
  if (query.categoryId) params.set('categoryId', query.categoryId)
  if (query.keyword.trim()) params.set('keyword', query.keyword.trim())
  try {
    activityPage.value = await get(`/activities?${params}`) || { records: [], total: 0, page, size: query.size }
    activities.value = activityPage.value.records || []
  } catch (error) {
    setNotice(error.message, 'error')
  } finally {
    loading.value = false
  }
}

async function openDetail(id, fromMySignups = false) {
  try {
    try {
      detail.value = await get(fromMySignups ? `/users/me/signups/${id}/detail` : `/activities/${id}`)
    } catch (error) {
      if (!fromMySignups || error.code !== 404) throw error
      detail.value = await get(`/activities/${id}`)
    }
    detailSignupState.value = { activityId: id, active: false, checkedIn: false }
    if (isStudent.value) {
      try {
        const state = await get(`/users/me/signups/${id}/state`)
        detailSignupState.value = { activityId: id, ...state }
      } catch {
        const signup = mySignups.value.records?.find(item => item.activityId === id)
        if (signup) detailSignupState.value = { activityId: id, active: signup.signupStatus === 1, checkedIn: signup.checkedIn }
      }
    }
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

function closeDetail() {
  detail.value = null
  detailSignupState.value = { activityId: null, active: false, checkedIn: false }
}

function openLogin() {
  authMode.value = 'login'
  authError.value = ''
  showAuth.value = true
}

function openRegister() {
  authMode.value = 'register'
  authError.value = ''
  showAuth.value = true
}

function closeAuth() {
  showAuth.value = false
  authError.value = ''
}

async function submitAuth() {
  authError.value = ''
  try {
    if (authMode.value === 'register') {
      await post('/users/register', {
        username: authForm.username,
        password: authForm.password,
        nickname: authForm.nickname || undefined
      })
      authMode.value = 'login'
      setNotice('注册成功，请使用新账号登录')
      return
    }

    const response = await post('/users/login', {
      username: authForm.username,
      password: authForm.password
    })
    localStorage.setItem('campus_token', response.token)
    localStorage.setItem('campus_user', JSON.stringify(response))
    user.value = response
    showAuth.value = false
    setNotice(`欢迎回来，${response.nickname || response.username}`)
    await refreshUserData()
  } catch (error) {
    authError.value = error.message
  }
}

function logout() {
  localStorage.removeItem('campus_token')
  localStorage.removeItem('campus_user')
  user.value = null
  view.value = 'activities'
  detail.value = null
  detailSignupState.value = { activityId: null, active: false, checkedIn: false }
  setNotice('已退出登录')
}

async function refreshUserData() {
  if (isStudent.value) await loadMySignups()
  if (isOrganizer.value) await loadOrganizerActivities()
  if (isAdmin.value) await Promise.all([loadAdminActivities(), loadOrganizerApplications()])
}

async function goView(nextView) {
  view.value = nextView
  window.scrollTo({ top: 0, behavior: 'smooth' })
  if (nextView === 'activities') await loadActivities(1)
  if (nextView === 'my-signups') await loadMySignups()
  if (nextView === 'organizer') await loadOrganizerActivities()
  if (nextView === 'admin') await Promise.all([loadAdminActivities(), loadOrganizerApplications()])
  if (nextView === 'profile') await loadProfile()
}

async function loadMySignups(page = mySignupPage.value) {
  try {
    mySignupPage.value = page
    mySignups.value = await get(`/users/me/signups?page=${page}&size=${pageSize}`) || { records: [], total: 0 }
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function signup(activityId) {
  if (!isLoggedIn.value) {
    openLogin()
    return
  }
  if (!isStudent.value) {
    setNotice('只有学生账号可以报名活动', 'error')
    return
  }
  try {
    await post(`/activities/${activityId}/signups`)
    setNotice('报名成功')
    await Promise.all([loadMySignups(), loadActivities(query.page), openDetail(activityId)])
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function cancelSignup(activityId) {
  const shouldRefreshDetail = detail.value?.id === activityId
  try {
    await remove(`/activities/${activityId}/signups`)
    setNotice('已取消报名')
    await Promise.all([
      loadMySignups(),
      loadActivities(query.page),
      ...(shouldRefreshDetail ? [openDetail(activityId)] : [])
    ])
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function checkin(activityId) {
  try {
    await post(`/activities/${activityId}/checkins`)
    setNotice('签到成功')
    await loadMySignups()
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function loadOrganizerActivities(page = 1) {
  try {
    organizerPage.value = page
    const status = organizerStatus.value ? `&status=${organizerStatus.value}` : ''
    organizerActivities.value = await get(`/organizer/activities?page=${page}&size=${pageSize}${status}`) || { records: [], total: 0 }
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function createActivity() {
  try {
    const body = {
      ...createForm,
      categoryId: Number(createForm.categoryId),
      maxParticipants: Number(createForm.maxParticipants)
    }
    if (editingActivityId.value) {
      await put(`/organizer/activities/${editingActivityId.value}`, body)
      setNotice('活动已修改并重新提交审核')
    } else {
      const id = await post('/organizer/activities', body)
      setNotice(`活动创建成功，编号为 ${id}`)
    }
    await Promise.all([loadOrganizerActivities(), loadActivities(1)])
    resetActivityForm()
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function editActivity(item) {
  try {
    const data = await get(`/organizer/activities/${item.id}`)
    editingActivityId.value = item.id
    editingAuditReason.value = item.auditReason || ''
    Object.assign(createForm, {
      title: data.title,
      description: data.description || '',
      coverUrl: data.coverUrl || '',
      location: data.location,
      categoryId: data.categoryId,
      maxParticipants: data.maxParticipants,
      registrationStartTime: apiTimeToInput(data.signupStartTime),
      registrationEndTime: apiTimeToInput(data.signupEndTime),
      startTime: apiTimeToInput(data.activityStartTime),
      endTime: apiTimeToInput(data.activityEndTime)
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

function resetActivityForm() {
  editingActivityId.value = null
  editingAuditReason.value = ''
  Object.assign(createForm, {
    title: '', description: '', coverUrl: '', location: '',
    categoryId: categories.value[0]?.id || 1, maxParticipants: 30,
    registrationStartTime: toInputDateTime(new Date()),
    registrationEndTime: toInputDateTime(addMinutes(new Date(), 60)),
    startTime: toInputDateTime(addMinutes(new Date(), 120)),
    endTime: toInputDateTime(addMinutes(new Date(), 240))
  })
}

async function loadParticipants(activity = participantActivity.value, page = 1) {
  participantActivity.value = activity
  participantPage.value = page
  try {
    participants.value = await get(`/organizer/activities/${activity.id}/signups?page=${page}&size=${pageSize}`) || { records: [], total: 0 }
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function cancelActivity(activityId) {
  try {
    await post(`/organizer/activities/${activityId}/cancel`)
    setNotice('活动已取消')
    await loadOrganizerActivities()
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function loadAdminActivities(page = 1) {
  try {
    adminPage.value = page
    const status = adminStatus.value ? `&status=${adminStatus.value}` : ''
    adminActivities.value = await get(`/admin/activities?page=${page}&size=${pageSize}${status}`) || { records: [], total: 0 }
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function loadOrganizerApplications() {
  try {
    pendingOrganizerApplications.value = await get('/admin/organizer-applications?page=1&size=20') || { records: [], total: 0 }
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function reviewOrganizerApplication(item, approved) {
  const reason = approved ? '符合组织者申请条件' : window.prompt('请输入拒绝原因：', '申请说明不够完整')
  if (!approved && !reason) return
  try {
    await post(`/admin/organizer-applications/${item.id}/review`, { approved, reason })
    setNotice(approved ? '已通过组织者申请' : '已拒绝组织者申请')
    await loadOrganizerApplications()
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function loadProfile() {
  try {
    const profile = await get('/users/me')
    Object.assign(profileForm, { nickname: profile.nickname || '', phone: profile.phone || '', avatar: profile.avatar || '' })
    organizerApplication.value = await get('/users/me/organizer-application')
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function applyForOrganizer() {
  try {
    await post('/users/me/organizer-application', { reason: organizerReason.value })
    organizerReason.value = ''
    organizerApplication.value = await get('/users/me/organizer-application')
    setNotice('组织者申请已提交')
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function saveProfile() {
  if (profilePhoneError.value) return
  try {
    const profile = await put('/users/me', { ...profileForm, nickname: profileForm.nickname.trim(), phone: profileForm.phone.trim() })
    user.value = { ...user.value, ...profile }
    localStorage.setItem('campus_user', JSON.stringify(user.value))
    setNotice('个人资料已保存')
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

async function handleImageUpload(event, target) {
  const file = event.target.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const url = await uploadImage(file)
    if (target === 'cover') createForm.coverUrl = url
    else profileForm.avatar = url
    setNotice('图片上传成功')
  } catch (error) {
    setNotice(error.message, 'error')
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

async function auditActivity(activityId, auditStatus) {
  try {
    const reason = auditStatus === 1 ? '活动信息完整，审核通过' : window.prompt('请输入拒绝原因：', '活动信息需要补充')
    if (auditStatus === 2 && !reason) return
    await post(`/admin/activities/${activityId}/audit`, {
      auditStatus,
      reason
    })
    setNotice(auditStatus === 1 ? '审核通过，活动已开放报名' : '已拒绝该活动')
    await Promise.all([loadAdminActivities(), loadActivities(1)])
  } catch (error) {
    setNotice(error.message, 'error')
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadActivities(1)])
  if (user.value) {
    try {
      const current = await get('/users/me')
      user.value = { ...user.value, ...current }
      localStorage.setItem('campus_user', JSON.stringify(user.value))
      await refreshUserData()
    } catch (error) {
      if (error.code === 401) logout()
    }
  }
})
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <a class="brand" href="#" @click.prevent="goView('activities')">
        <span class="brand-mark">CH</span>
        <span>Campus<span>Hub</span></span>
      </a>
      <nav class="main-nav" aria-label="主导航">
        <button :class="{ active: view === 'activities' }" @click="goView('activities')">发现活动</button>
        <button v-if="isStudent" :class="{ active: view === 'my-signups' }" @click="goView('my-signups')">我的报名</button>
        <button v-if="isOrganizer" :class="{ active: view === 'organizer' }" @click="goView('organizer')">组织者工作台</button>
        <button v-if="isAdmin" :class="{ active: view === 'admin' }" @click="goView('admin')">审核管理</button>
        <button v-if="isLoggedIn" :class="{ active: view === 'profile' }" @click="goView('profile')">个人中心</button>
      </nav>
      <div class="account-area">
        <template v-if="isLoggedIn">
          <span class="role-pill">{{ roleLabel(user.role) }}</span>
          <span class="account-name">{{ user.nickname || user.username }}</span>
          <button class="ghost-button" @click="logout">退出</button>
        </template>
        <template v-else>
          <button class="ghost-button" @click="openLogin">登录</button>
          <button class="primary-button small" @click="openRegister">注册</button>
        </template>
      </div>
    </header>

    <main class="page-wrap">
      <transition name="toast">
        <div v-if="notice" class="toast" :class="noticeType">{{ notice }}</div>
      </transition>

      <section v-if="view === 'activities'" class="hero">
        <div class="hero-copy">
          <p class="eyebrow">CAMPUS LIFE, TOGETHER</p>
          <h1>把校园里的<br /><em>每一次相遇</em>变成故事。</h1>
          <p class="hero-description">发现感兴趣的活动，认识志同道合的伙伴，让课表之外的时间也充满期待。</p>
          <div class="hero-actions">
            <button class="primary-button" @click="scrollToActivities">浏览活动 <span>↘</span></button>
            <button v-if="!isLoggedIn" class="text-button" @click="openRegister">创建账号 <span>→</span></button>
          </div>
        </div>
        <div class="hero-art" aria-hidden="true">
          <div class="orbit orbit-one"></div>
          <div class="orbit orbit-two"></div>
          <div class="hero-card floating-card card-top"><span>✦</span><strong>今日推荐</strong><small>总有一场活动适合你</small></div>
          <div class="hero-card floating-card card-bottom"><span class="mini-avatar">✺</span><div><strong>一起参加</strong><small>和校园里的朋友见面</small></div></div>
          <div class="sun-shape"><span>CH</span></div>
        </div>
      </section>

      <section v-if="view === 'activities'" class="activity-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">EXPLORE</p>
            <h2>正在发生的活动</h2>
          </div>
          <span class="result-count">共 {{ activityPage.total || 0 }} 场</span>
        </div>

        <form class="filter-bar" @submit.prevent="loadActivities(1)">
          <label class="search-field"><span>⌕</span><input v-model="query.keyword" placeholder="搜索活动标题" /></label>
          <label class="select-field"><span>分类</span><select v-model="query.categoryId" @change="loadActivities(1)"><option value="">全部分类</option><option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option></select></label>
          <button class="primary-button filter-button" type="submit">搜索活动</button>
        </form>

        <div v-if="loading" class="activity-grid">
          <div v-for="index in 3" :key="index" class="activity-card skeleton-card"><div class="skeleton-image"></div><div class="skeleton-line wide"></div><div class="skeleton-line"></div></div>
        </div>
        <div v-else-if="activities.length" class="activity-grid">
          <article v-for="(activity, index) in activities" :key="activity.id" class="activity-card" @click="openDetail(activity.id)">
            <div class="activity-cover" :class="`cover-${index % 4}`">
              <img v-if="activity.coverUrl" :src="activity.coverUrl" :alt="activity.title" />
              <span v-else class="cover-symbol">{{ ['✦', '◒', '✺', '✿'][index % 4] }}</span>
              <span class="status-badge" :class="statusClass(activity.status)">{{ statusLabel(activity.status) }}</span>
            </div>
            <div class="activity-content">
              <div class="activity-meta"><span class="category-chip">{{ categoryName(activity.categoryId) }}</span><span class="activity-date">{{ formatDate(activity.activityStartTime) }}</span></div>
              <h3>{{ activity.title }}</h3>
              <p class="activity-location">⌖ {{ activity.location }}</p>
              <button class="inline-button" @click.stop="openDetail(activity.id)">查看详情 <span>→</span></button>
            </div>
          </article>
        </div>
        <div v-else class="empty-state"><span>☼</span><h3>目前没有开放报名的活动</h3><p>组织者发布并经管理员审核后，活动会显示在这里。</p></div>

        <div v-if="activityPage.total > query.size" class="pagination">
          <button :disabled="query.page <= 1" @click="loadActivities(query.page - 1)">←</button>
          <span>第 {{ query.page }} / {{ totalPages }} 页</span>
          <button :disabled="query.page >= totalPages" @click="loadActivities(query.page + 1)">→</button>
        </div>
      </section>

      <section v-if="view === 'my-signups' && isStudent" class="dashboard-section">
        <div class="section-heading"><div><p class="eyebrow">MY CAMPUS</p><h2>我的报名</h2></div><span class="result-count">{{ mySignups.total || 0 }} 条记录</span></div>
        <div v-if="mySignups.records?.length" class="table-card">
          <div v-for="item in mySignups.records" :key="item.signupId" class="signup-row">
            <div class="signup-summary" @click="openDetail(item.activityId, true)"><strong>{{ item.title }}</strong><small>⌖ {{ item.location }} · {{ formatDate(item.activityStartTime) }}</small><button class="inline-button">查看活动 →</button></div>
            <span class="status-badge" :class="statusClass(item.activityStatus)">{{ signupStatusLabel(item) }}</span>
            <div v-if="item.signupStatus === 1 && item.activityStatus === 2" class="row-actions"><button v-if="canCheckin(item)" class="mini-button approve" @click="checkin(item.activityId)">立即签到</button><button class="mini-button danger" @click="cancelSignup(item.activityId)">取消报名</button></div>
          </div>
        </div>
        <div v-else class="empty-state compact"><span>♡</span><h3>还没有报名活动</h3><p>报名成功后，活动会出现在这里。</p><button class="primary-button small empty-action" @click="goView('activities')">去发现活动</button></div>
        <div v-if="mySignups.total > pageSize" class="pagination"><button :disabled="mySignupPage <= 1" @click="loadMySignups(mySignupPage - 1)">←</button><span>第 {{ mySignupPage }} / {{ mySignupPages }} 页</span><button :disabled="mySignupPage >= mySignupPages" @click="loadMySignups(mySignupPage + 1)">→</button></div>
      </section>

      <section v-if="view === 'organizer' && isOrganizer" class="dashboard-section">
        <div class="section-heading"><div><p class="eyebrow">ORGANIZER SPACE</p><h2>组织者工作台</h2></div><label class="compact-filter">活动状态<select v-model="organizerStatus" @change="loadOrganizerActivities"><option value="">全部</option><option value="1">待审核</option><option value="2">报名中</option><option value="3">已结束</option><option value="4">已取消</option><option value="5">审核拒绝</option></select></label></div>
        <div class="dashboard-grid">
          <form class="form-card" @submit.prevent="createActivity">
            <div class="card-heading"><div><p class="eyebrow">{{ editingActivityId ? 'EDIT EVENT' : 'NEW EVENT' }}</p><h3>{{ editingActivityId ? '修改并重新提交' : '发布新活动' }}</h3></div><button v-if="editingActivityId" type="button" class="mini-button" @click="resetActivityForm">取消编辑</button><span v-else class="card-icon">＋</span></div>
            <div v-if="editingActivityId && editingAuditReason" class="audit-feedback"><strong>管理员审核意见</strong><p>{{ editingAuditReason }}</p></div>
            <label>活动标题<input v-model="createForm.title" required maxlength="100" placeholder="例如：春日草地音乐会" /></label>
            <label>活动描述<textarea v-model="createForm.description" rows="3" placeholder="介绍活动亮点和注意事项"></textarea></label>
            <div class="form-row"><label>地点<input v-model="createForm.location" required placeholder="活动地点" /></label><label>分类<select v-model="createForm.categoryId" required><option v-for="category in categories" :key="category.id" :value="category.id">{{ category.name }}</option></select></label></div>
            <div class="form-row"><label>人数上限<input v-model="createForm.maxParticipants" type="number" min="1" required /></label><label>活动封面<input type="file" accept="image/jpeg,image/png,image/webp,image/gif" @change="handleImageUpload($event, 'cover')" /><small v-if="uploading">正在上传...</small></label></div>
            <div v-if="createForm.coverUrl" class="upload-preview"><img :src="createForm.coverUrl" alt="活动封面预览" /><button type="button" class="mini-button danger" @click="createForm.coverUrl = ''">移除封面</button></div>
            <div class="form-row"><label>报名开始<input v-model="createForm.registrationStartTime" type="datetime-local" required /></label><label>报名截止<input v-model="createForm.registrationEndTime" type="datetime-local" required /></label></div>
            <div class="form-row"><label>活动开始<input v-model="createForm.startTime" type="datetime-local" required /></label><label>活动结束<input v-model="createForm.endTime" type="datetime-local" required /></label></div>
            <button class="primary-button full" type="submit">{{ editingActivityId ? '保存并重新审核' : '提交审核' }} <span>→</span></button>
          </form>
          <div class="table-card activity-management"><div class="card-heading"><div><p class="eyebrow">MY EVENTS</p><h3>我发布的活动</h3></div><span class="result-count">{{ organizerActivities.total || 0 }} 场</span></div>
            <template v-if="organizerActivities.records?.length"><div v-for="item in organizerActivities.records" :key="item.id" class="management-row"><div class="management-summary"><strong>{{ item.title }}</strong><small>{{ formatDate(item.activityStartTime) }} · {{ item.currentParticipants }}/{{ item.maxParticipants }} 人</small><p v-if="item.status === 5 && item.auditReason" class="reject-reason">拒绝原因：{{ item.auditReason }}</p></div><span class="status-badge" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span><div class="row-actions"><button class="mini-button" @click="loadParticipants(item)">报名名单</button><button v-if="item.status === 1 || item.status === 5" class="mini-button" @click="editActivity(item)">编辑</button><button v-if="item.status === 1 || item.status === 2" class="mini-button danger" @click="cancelActivity(item.id)">取消</button></div></div></template>
            <div v-else class="empty-state compact"><span>✦</span><h3>还没有发布活动</h3><p>填写左侧表单，发布你的第一个活动。</p></div>
            <div v-if="organizerActivities.total > pageSize" class="pagination compact-pagination"><button :disabled="organizerPage <= 1" @click="loadOrganizerActivities(organizerPage - 1)">←</button><span>第 {{ organizerPage }} / {{ organizerPages }} 页</span><button :disabled="organizerPage >= organizerPages" @click="loadOrganizerActivities(organizerPage + 1)">→</button></div>
          </div>
        </div>
      </section>

      <section v-if="view === 'admin' && isAdmin" class="dashboard-section">
        <div class="section-heading"><div><p class="eyebrow">ADMIN CONSOLE</p><h2>活动审核</h2></div><div class="heading-actions"><label class="compact-filter">活动状态<select v-model="adminStatus" @change="loadAdminActivities"><option value="1">待审核</option><option value="">全部</option><option value="2">报名中</option><option value="3">已结束</option><option value="4">已取消</option><option value="5">审核拒绝</option></select></label><span class="result-count">{{ adminActivities.total || 0 }} 场活动</span></div></div>
        <div class="table-card admin-table"><template v-if="adminActivities.records?.length"><div v-for="item in adminActivities.records" :key="item.id" class="management-row"><div><strong>#{{ item.id }} · {{ item.title }}</strong><small>组织者 ID：{{ item.organizerId }} · 创建于 {{ formatDate(item.createdAt) }}</small></div><span class="status-badge" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span><div v-if="item.status === 1" class="row-actions"><button class="mini-button approve" @click="auditActivity(item.id, 1)">通过</button><button class="mini-button danger" @click="auditActivity(item.id, 2)">拒绝</button></div></div></template><div v-else class="empty-state compact"><span>✓</span><h3>暂无待审核活动</h3><p>所有活动都处理完了。</p></div></div>
        <div v-if="adminActivities.total > pageSize" class="pagination"><button :disabled="adminPage <= 1" @click="loadAdminActivities(adminPage - 1)">←</button><span>第 {{ adminPage }} / {{ adminPages }} 页</span><button :disabled="adminPage >= adminPages" @click="loadAdminActivities(adminPage + 1)">→</button></div>
        <div class="section-heading sub-heading"><div><p class="eyebrow">ROLE APPLICATIONS</p><h2>组织者申请</h2></div><span class="result-count">{{ pendingOrganizerApplications.total || 0 }} 条待处理</span></div>
        <div class="table-card"><template v-if="pendingOrganizerApplications.records?.length"><div v-for="item in pendingOrganizerApplications.records" :key="item.id" class="management-row application-row"><div><strong>{{ item.nickname || item.username }}（@{{ item.username }}）</strong><small>{{ formatDate(item.createdAt) }}</small><p>{{ item.reason }}</p></div><div class="row-actions"><button class="mini-button approve" @click="reviewOrganizerApplication(item, true)">通过</button><button class="mini-button danger" @click="reviewOrganizerApplication(item, false)">拒绝</button></div></div></template><div v-else class="empty-state compact"><span>✓</span><h3>暂无组织者申请</h3><p>新的申请会显示在这里。</p></div></div>
      </section>

      <section v-if="view === 'profile' && isLoggedIn" class="dashboard-section profile-section">
        <div class="section-heading"><div><p class="eyebrow">MY PROFILE</p><h2>个人中心</h2></div><span class="role-pill">{{ roleLabel(user.role) }}</span></div>
        <form class="form-card profile-card" @submit.prevent="saveProfile">
          <div class="profile-avatar"><img v-if="profileForm.avatar" :src="profileForm.avatar" alt="头像" /><span v-else>{{ (profileForm.nickname || user.username).slice(0, 1) }}</span></div>
          <div class="profile-fields"><label>用户名<input :value="user.username" disabled /></label><label>昵称<input v-model="profileForm.nickname" required maxlength="50" /></label><label>手机号（可选）<input v-model="profileForm.phone" type="tel" inputmode="numeric" autocomplete="tel" maxlength="11" placeholder="例如：13800138000" :aria-invalid="Boolean(profilePhoneError)" aria-describedby="profile-phone-help" /><small id="profile-phone-help" class="field-help" :class="{ invalid: profilePhoneError }">{{ profilePhoneError || '只接受 11 位大陆手机号；没有手机号可以留空。' }}</small></label><label>更换头像<input type="file" accept="image/jpeg,image/png,image/webp,image/gif" @change="handleImageUpload($event, 'avatar')" /></label><button class="primary-button" type="submit" :disabled="Boolean(profilePhoneError)">保存资料</button></div>
        </form>
        <div v-if="isStudent" class="form-card organizer-application-card">
          <div class="card-heading"><div><p class="eyebrow">BECOME AN ORGANIZER</p><h3>申请成为组织者</h3></div></div>
          <template v-if="!organizerApplication || organizerApplication.status === 3"><p class="muted-text">通过后可以发布和管理校园活动。请简单说明你的组织、社团或活动计划。</p><p v-if="organizerApplication?.status === 3" class="reject-reason">上次申请被拒绝：{{ organizerApplication.reviewReason }}</p><textarea v-model="organizerReason" rows="4" maxlength="500" placeholder="至少10个字符，例如：我是摄影社负责人，希望发布社团活动。"></textarea><button class="primary-button" :disabled="organizerReason.trim().length < 10" @click="applyForOrganizer">提交申请</button></template>
          <div v-else-if="organizerApplication.status === 1" class="application-status pending"><strong>申请审核中</strong><p>{{ organizerApplication.reason }}</p><small>提交于 {{ formatDate(organizerApplication.createdAt) }}</small></div>
          <div v-else class="application-status approved"><strong>申请已通过</strong><p>账号已经获得组织者权限，刷新页面即可进入组织者工作台。</p></div>
        </div>
      </section>

      <section v-if="view === 'activities'" class="features-section">
        <div class="section-heading"><div><p class="eyebrow">HOW IT WORKS</p><h2>CampusHub 能做什么</h2></div></div>
        <div class="feature-grid">
          <article><span class="feature-icon">01</span><p class="feature-role">学生</p><h3>发现并参加校园活动</h3><p>按分类和关键词寻找活动，查看时间、地点和名额，在线报名、取消报名，并在“我的报名”中查看全部记录。</p></article>
          <article><span class="feature-icon">02</span><p class="feature-role">组织者</p><h3>发布并管理活动</h3><p>填写活动资料并提交审核，查看自己发布的活动、报名人数和学生名单，也可以在活动开始前取消活动。</p></article>
          <article><span class="feature-icon">03</span><p class="feature-role">管理员</p><h3>审核和维护平台内容</h3><p>查看所有活动，对待审核活动执行通过或拒绝。通过后活动自动出现在公开列表，供学生报名。</p></article>
        </div>
        <div class="flow-card"><strong>完整流程</strong><span>组织者发布</span><b>→</b><span>管理员审核</span><b>→</b><span>学生报名</span><b>→</b><span>活动签到</span><b>→</b><span>自动结束</span></div>
      </section>
    </main>

    <footer class="footer"><span>CampusHub</span><span>校园活动，从这里开始。</span><span>© 2026</span></footer>

    <teleport to="body">
      <div v-if="participantActivity" class="modal-backdrop" @click.self="participantActivity = null">
        <section class="participants-modal"><button class="modal-close" @click="participantActivity = null">×</button><div class="participants-heading"><div><p class="eyebrow">PARTICIPANTS</p><h2>{{ participantActivity.title }}</h2><span>{{ participants.total || 0 }} 条报名记录</span></div></div><div class="participant-list"><template v-if="participants.records?.length"><div v-for="person in participants.records" :key="person.signupId" class="participant-row"><span class="avatar">{{ (person.nickname || person.username || '?').slice(0, 1) }}</span><div><strong>{{ person.nickname || person.username }}</strong><small>@{{ person.username }} · {{ formatDate(person.signupTime) }}</small></div><span class="checkin-state" :class="{ done: person.checkedIn }">{{ person.checkedIn ? '已签到' : '未签到' }}</span></div></template><div v-else class="empty-state compact"><span>♡</span><h3>暂时没有报名</h3><p>学生报名后会直接显示在这里。</p></div></div><div v-if="participants.total > pageSize" class="pagination compact-pagination"><button :disabled="participantPage <= 1" @click="loadParticipants(participantActivity, participantPage - 1)">←</button><span>第 {{ participantPage }} / {{ participantPages }} 页</span><button :disabled="participantPage >= participantPages" @click="loadParticipants(participantActivity, participantPage + 1)">→</button></div></section>
      </div>
      <div v-if="showAuth" class="modal-backdrop" @click.self="closeAuth">
        <section class="auth-modal">
          <button class="modal-close" @click="closeAuth">×</button>
          <div class="auth-illustration"><span>CH</span><p>让校园生活<br /><em>不止于课表</em></p></div>
          <div class="auth-content"><p class="eyebrow">WELCOME TO CAMPUSHUB</p><h2>{{ authMode === 'login' ? '欢迎回来' : '注册学生账号' }}</h2><p class="auth-subtitle">{{ authMode === 'login' ? '登录后即可报名活动，记录你的校园时光。' : '新账号默认为学生；登录后可在个人中心申请成为组织者。' }}</p><div class="auth-tabs"><button :class="{ active: authMode === 'login' }" @click="authMode = 'login'">登录</button><button :class="{ active: authMode === 'register' }" @click="authMode = 'register'">学生注册</button></div><form @submit.prevent="submitAuth"><label>用户名<input v-model="authForm.username" required minlength="4" placeholder="4-20位字母、数字或下划线" /></label><label>密码<input v-model="authForm.password" type="password" required minlength="6" placeholder="至少6位密码" /></label><label v-if="authMode === 'register'">昵称（可选）<input v-model="authForm.nickname" placeholder="在活动中展示的名字" /></label><p v-if="authError" class="form-error">{{ authError }}</p><button class="primary-button full" type="submit">{{ authMode === 'login' ? '登录 CampusHub' : '注册学生账号' }} <span>→</span></button></form></div>
        </section>
      </div>

      <div v-if="detail" class="modal-backdrop" @click.self="closeDetail">
        <section class="detail-modal">
          <button class="modal-close" @click="closeDetail">×</button>
          <div class="detail-cover" :class="`cover-${(detail.id || 0) % 4}`">
            <img v-if="detail.coverUrl" :src="detail.coverUrl" :alt="detail.title" /><span v-else class="cover-symbol large">✦</span>
            <span class="status-badge" :class="statusClass(detail.status)">{{ statusLabel(detail.status) }}</span>
          </div>
          <div class="detail-content">
            <p class="activity-date">{{ formatDate(detail.activityStartTime) }}</p><h2>{{ detail.title }}</h2>
            <p class="detail-description">{{ detail.description || '组织者暂未填写活动介绍。' }}</p>
            <div class="detail-meta"><span>⌖ {{ detail.location }}</span><span>♙ {{ detail.currentParticipants || 0 }} / {{ detail.maxParticipants }} 人</span><span>报名截止：{{ formatDate(detail.signupEndTime) }}</span></div>
            <div class="detail-actions">
              <template v-if="isStudent && detail.status === 2">
                <p v-if="hasActiveSignup(detail.id) && detailSignupState.checkedIn" class="muted-text">已报名并签到</p>
                <button v-else-if="hasActiveSignup(detail.id) && canCancelSignup(detail)" class="primary-button" @click="cancelSignup(detail.id)">取消报名 <span>×</span></button>
                <p v-else-if="hasActiveSignup(detail.id)" class="muted-text">已报名，当前无法取消</p>
                <p v-else-if="signupIssue(detail)" class="muted-text">{{ signupIssue(detail) }}</p>
                <button v-else class="primary-button" @click="signup(detail.id)">立即报名 <span>→</span></button>
              </template>
              <button v-else-if="!isLoggedIn" class="primary-button" @click="openLogin">登录后报名 <span>→</span></button>
              <p v-else-if="detail.status !== 2" class="muted-text">当前活动不在报名状态</p>
              <p v-else class="muted-text">当前账号不能报名学生活动</p>
            </div>
          </div>
        </section>
      </div>
    </teleport>
  </div>
</template>
