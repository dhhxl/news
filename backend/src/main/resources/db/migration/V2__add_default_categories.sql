-- V2__add_default_categories.sql
-- 插入6个系统默认新闻分类

INSERT INTO categories (name, description, is_default, created_at, created_by) VALUES
('政治', '国内外政治新闻、政策法规等', TRUE, CURRENT_TIMESTAMP, NULL),
('经济', '经济动态、财经资讯、市场行情等', TRUE, CURRENT_TIMESTAMP, NULL),
('体育', '体育赛事、运动新闻、体育明星等', TRUE, CURRENT_TIMESTAMP, NULL),
('娱乐', '娱乐圈动态、影视资讯、明星八卦等', TRUE, CURRENT_TIMESTAMP, NULL),
('科技', '科技创新、数码产品、互联网资讯等', TRUE, CURRENT_TIMESTAMP, NULL),
('社会', '社会民生、热点事件、人文故事等', TRUE, CURRENT_TIMESTAMP, NULL);

