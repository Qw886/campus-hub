INSERT INTO activity_category (name, sort, status)
SELECT '体育竞赛', 10, 1 WHERE NOT EXISTS (SELECT 1 FROM activity_category WHERE name = '体育竞赛');
INSERT INTO activity_category (name, sort, status)
SELECT '学术讲座', 20, 1 WHERE NOT EXISTS (SELECT 1 FROM activity_category WHERE name = '学术讲座');
INSERT INTO activity_category (name, sort, status)
SELECT '社团活动', 30, 1 WHERE NOT EXISTS (SELECT 1 FROM activity_category WHERE name = '社团活动');
