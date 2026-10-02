INSERT INTO activity_category (name, sort, status)
SELECT '体育竞赛', 10, 1 WHERE NOT EXISTS (SELECT 1 FROM activity_category WHERE name = '体育竞赛');
INSERT INTO activity_category (name, sort, status)
SELECT '学术讲座', 20, 1 WHERE NOT EXISTS (SELECT 1 FROM activity_category WHERE name = '学术讲座');
INSERT INTO activity_category (name, sort, status)
SELECT '社团活动', 30, 1 WHERE NOT EXISTS (SELECT 1 FROM activity_category WHERE name = '社团活动');

-- 首次部署时提供可直接浏览的示例活动。
-- 该账号仅作为示例活动的归属人，已禁用登录，不是管理员，也不会占用真实用户账号。
INSERT INTO sys_user (username, password, nickname, role, status)
SELECT 'campushub_demo', '!demo-account-disabled', 'CampusHub 示例组织者', 'ORGANIZER', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'campushub_demo');

SET @demo_organizer_id = (SELECT id FROM sys_user WHERE username = 'campushub_demo' LIMIT 1);
SET @sports_category_id = (SELECT id FROM activity_category WHERE name = '体育竞赛' LIMIT 1);
SET @lecture_category_id = (SELECT id FROM activity_category WHERE name = '学术讲座' LIMIT 1);
SET @club_category_id = (SELECT id FROM activity_category WHERE name = '社团活动' LIMIT 1);

INSERT INTO activity (
    organizer_id, category_id, title, description, cover_url, location,
    signup_start_time, signup_end_time, activity_start_time, activity_end_time,
    max_participants, current_participants, status
)
SELECT @demo_organizer_id, @sports_category_id,
       '校园篮球友谊赛',
       '欢迎同学组队参加校园篮球交流活动，现场签到入场。',
       '', '学校体育馆',
       DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY),
       DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY) + INTERVAL 2 HOUR,
       40, 0, 2
WHERE @demo_organizer_id IS NOT NULL
  AND @sports_category_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM activity WHERE title = '校园篮球友谊赛');

INSERT INTO activity (
    organizer_id, category_id, title, description, cover_url, location,
    signup_start_time, signup_end_time, activity_start_time, activity_end_time,
    max_participants, current_participants, status
)
SELECT @demo_organizer_id, @lecture_category_id,
       'AI 技术分享会',
       '面向在校学生的人工智能技术分享，欢迎对编程和 AI 感兴趣的同学参加。',
       '', '教学楼 A201',
       DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY),
       DATE_ADD(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY) + INTERVAL 2 HOUR,
       100, 0, 2
WHERE @demo_organizer_id IS NOT NULL
  AND @lecture_category_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM activity WHERE title = 'AI 技术分享会');

INSERT INTO activity (
    organizer_id, category_id, title, description, cover_url, location,
    signup_start_time, signup_end_time, activity_start_time, activity_end_time,
    max_participants, current_participants, status
)
SELECT @demo_organizer_id, @club_category_id,
       '摄影社校园采风',
       '带上手机或相机，一起记录校园风景，和同学交流摄影心得。',
       '', '图书馆南门',
       DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 6 DAY),
       DATE_ADD(NOW(), INTERVAL 8 DAY), DATE_ADD(NOW(), INTERVAL 8 DAY) + INTERVAL 3 HOUR,
       30, 0, 2
WHERE @demo_organizer_id IS NOT NULL
  AND @club_category_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM activity WHERE title = '摄影社校园采风');
