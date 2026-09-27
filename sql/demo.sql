-- CampusHub 本地演示数据，可重复执行。请勿用于生产环境。
-- 请先在网页注册：student001/123456、test_org/123456、admin_test/Test123456。
USE campus_hub;

UPDATE sys_user SET role='USER', status=1 WHERE username='student001';
UPDATE sys_user SET role='ORGANIZER', status=1 WHERE username='test_org';
UPDATE sys_user SET role='ADMIN', status=1 WHERE username='admin_test';

SET @student_id=(SELECT id FROM sys_user WHERE username='student001' LIMIT 1);
SET @organizer_id=(SELECT id FROM sys_user WHERE username='test_org' LIMIT 1);
SET @sports_id=(SELECT id FROM activity_category WHERE name='体育竞赛' LIMIT 1);
SET @lecture_id=(SELECT id FROM activity_category WHERE name='学术讲座' LIMIT 1);
SET @club_id=(SELECT id FROM activity_category WHERE name='社团活动' LIMIT 1);

-- title, category, description, location, max people, start days, status
INSERT INTO activity (organizer_id,category_id,title,description,cover_url,location,
 signup_start_time,signup_end_time,activity_start_time,activity_end_time,
 max_participants,current_participants,status,created_at,updated_at)
SELECT @organizer_id,@sports_id,'[演示] 周末篮球友谊赛','全校学生篮球交流赛，请穿运动鞋并提前十分钟到场。','',
 '学校体育馆',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 5 DAY),
 DATE_ADD(NOW(),INTERVAL 7 DAY),DATE_ADD(DATE_ADD(NOW(),INTERVAL 7 DAY),INTERVAL 2 HOUR),
 20,0,2,NOW(),NOW()
WHERE @organizer_id IS NOT NULL AND @sports_id IS NOT NULL
 AND NOT EXISTS(SELECT 1 FROM activity WHERE organizer_id=@organizer_id AND title='[演示] 周末篮球友谊赛');

INSERT INTO activity (organizer_id,category_id,title,description,cover_url,location,
 signup_start_time,signup_end_time,activity_start_time,activity_end_time,
 max_participants,current_participants,status,created_at,updated_at)
SELECT @organizer_id,@lecture_id,'[演示] 人工智能公开课','介绍人工智能基础概念、应用场景和学习路线。','',
 '教学楼 A201',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 8 DAY),
 DATE_ADD(NOW(),INTERVAL 10 DAY),DATE_ADD(DATE_ADD(NOW(),INTERVAL 10 DAY),INTERVAL 2 HOUR),
 80,0,2,NOW(),NOW()
WHERE @organizer_id IS NOT NULL AND @lecture_id IS NOT NULL
 AND NOT EXISTS(SELECT 1 FROM activity WHERE organizer_id=@organizer_id AND title='[演示] 人工智能公开课');

INSERT INTO activity (organizer_id,category_id,title,description,cover_url,location,
 signup_start_time,signup_end_time,activity_start_time,activity_end_time,
 max_participants,current_participants,status,created_at,updated_at)
SELECT @organizer_id,@club_id,'[演示] 摄影社校园采风','带上手机或相机，一起记录校园风景。','',
 '图书馆南门',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 3 DAY),
 DATE_ADD(NOW(),INTERVAL 4 DAY),DATE_ADD(DATE_ADD(NOW(),INTERVAL 4 DAY),INTERVAL 3 HOUR),
 30,0,2,NOW(),NOW()
WHERE @organizer_id IS NOT NULL AND @club_id IS NOT NULL
 AND NOT EXISTS(SELECT 1 FROM activity WHERE organizer_id=@organizer_id AND title='[演示] 摄影社校园采风');

-- 学生已默认报名，开始时间在二十分钟后，可立即测试签到。
INSERT INTO activity (organizer_id,category_id,title,description,cover_url,location,
 signup_start_time,signup_end_time,activity_start_time,activity_end_time,
 max_participants,current_participants,status,created_at,updated_at)
SELECT @organizer_id,@club_id,'[演示] 即时签到测试活动','学生账号已默认报名，用于测试我的报名和签到。','',
 '大学生活动中心',DATE_SUB(NOW(),INTERVAL 10 MINUTE),DATE_ADD(NOW(),INTERVAL 10 MINUTE),
 DATE_ADD(NOW(),INTERVAL 20 MINUTE),DATE_ADD(NOW(),INTERVAL 2 HOUR),
 50,0,2,NOW(),NOW()
WHERE @organizer_id IS NOT NULL AND @club_id IS NOT NULL
 AND NOT EXISTS(SELECT 1 FROM activity WHERE organizer_id=@organizer_id AND title='[演示] 即时签到测试活动');

-- 管理员可以在审核管理中处理这个活动。
INSERT INTO activity (organizer_id,category_id,title,description,cover_url,location,
 signup_start_time,signup_end_time,activity_start_time,activity_end_time,
 max_participants,current_participants,status,created_at,updated_at)
SELECT @organizer_id,@club_id,'[演示] 待审核志愿活动','等待管理员审核的校园志愿服务活动。','',
 '校园服务中心',DATE_ADD(NOW(),INTERVAL 1 DAY),DATE_ADD(NOW(),INTERVAL 6 DAY),
 DATE_ADD(NOW(),INTERVAL 8 DAY),DATE_ADD(DATE_ADD(NOW(),INTERVAL 8 DAY),INTERVAL 4 HOUR),
 40,0,1,NOW(),NOW()
WHERE @organizer_id IS NOT NULL AND @club_id IS NOT NULL
 AND NOT EXISTS(SELECT 1 FROM activity WHERE organizer_id=@organizer_id AND title='[演示] 待审核志愿活动');

-- 重复执行时刷新两个关键活动的时间，避免演示数据过期。
UPDATE activity SET signup_start_time=DATE_SUB(NOW(),INTERVAL 1 DAY),
 signup_end_time=DATE_ADD(NOW(),INTERVAL 5 DAY),activity_start_time=DATE_ADD(NOW(),INTERVAL 7 DAY),
 activity_end_time=DATE_ADD(DATE_ADD(NOW(),INTERVAL 7 DAY),INTERVAL 2 HOUR),status=2,updated_at=NOW()
WHERE organizer_id=@organizer_id AND title='[演示] 周末篮球友谊赛';

UPDATE activity SET signup_start_time=DATE_SUB(NOW(),INTERVAL 10 MINUTE),
 signup_end_time=DATE_ADD(NOW(),INTERVAL 10 MINUTE),activity_start_time=DATE_ADD(NOW(),INTERVAL 20 MINUTE),
 activity_end_time=DATE_ADD(NOW(),INTERVAL 2 HOUR),status=2,updated_at=NOW()
WHERE organizer_id=@organizer_id AND title='[演示] 即时签到测试活动';

SET @basketball_id=(SELECT id FROM activity WHERE organizer_id=@organizer_id AND title='[演示] 周末篮球友谊赛' LIMIT 1);
SET @checkin_id=(SELECT id FROM activity WHERE organizer_id=@organizer_id AND title='[演示] 即时签到测试活动' LIMIT 1);

INSERT INTO activity_signup (user_id,activity_id,status,signup_time,cancel_time)
SELECT @student_id,@basketball_id,1,NOW(),NULL WHERE @student_id IS NOT NULL AND @basketball_id IS NOT NULL
ON DUPLICATE KEY UPDATE status=1,signup_time=NOW(),cancel_time=NULL;

INSERT INTO activity_signup (user_id,activity_id,status,signup_time,cancel_time)
SELECT @student_id,@checkin_id,1,NOW(),NULL WHERE @student_id IS NOT NULL AND @checkin_id IS NOT NULL
ON DUPLICATE KEY UPDATE status=1,signup_time=NOW(),cancel_time=NULL;

UPDATE activity a SET current_participants=(
 SELECT COUNT(*) FROM activity_signup s WHERE s.activity_id=a.id AND s.status=1
) WHERE a.organizer_id=@organizer_id AND a.title LIKE '[演示] %';

SELECT id,title,status,current_participants,activity_start_time FROM activity
WHERE organizer_id=@organizer_id AND title LIKE '[演示] %' ORDER BY id;
