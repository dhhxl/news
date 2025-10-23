-- V6__add_default_classification_rules.sql
-- 添加默认分类规则

-- 关键词分类规则
-- 注意：表字段为 keywords（不是 rule_condition），created_by 必填
INSERT INTO classification_rules (rule_type, keywords, target_category_id, priority, is_enabled, created_by, created_at) VALUES
-- 政治类关键词
('KEYWORD', '政府,国务院,外交,政策,党,习近平,李强', 1, 10, TRUE, 1, CURRENT_TIMESTAMP),
-- 经济类关键词
('KEYWORD', '经济,金融,股市,GDP,投资,银行,财政', 2, 20, TRUE, 1, CURRENT_TIMESTAMP),
-- 体育类关键词
('KEYWORD', '足球,篮球,NBA,CBA,世界杯,奥运,体育,运动', 3, 30, TRUE, 1, CURRENT_TIMESTAMP),
-- 娱乐类关键词
('KEYWORD', '电影,明星,娱乐,演唱会,综艺,票房,影视', 4, 40, TRUE, 1, CURRENT_TIMESTAMP),
-- 科技类关键词
('KEYWORD', 'AI,人工智能,科技,互联网,5G,芯片,华为,小米,苹果', 5, 50, TRUE, 1, CURRENT_TIMESTAMP);

-- 来源预设规则（作为备用）
INSERT INTO classification_rules (rule_type, source_pattern, target_category_id, priority, is_enabled, created_by, created_at) VALUES
('SOURCE', 'SINA', 6, 100, TRUE, 1, CURRENT_TIMESTAMP),
('SOURCE', 'CCTV', 1, 100, TRUE, 1, CURRENT_TIMESTAMP),
('SOURCE', 'NETEASE', 5, 100, TRUE, 1, CURRENT_TIMESTAMP);

-- 说明：
-- 1. 关键词规则优先级更高（10-50）
-- 2. 来源规则优先级较低（100）
-- 3. 如果都不匹配，默认分类为"社会"（ID=6）

