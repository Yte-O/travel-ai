DROP DATABASE IF EXISTS gjq_db;
CREATE DATABASE IF NOT EXISTS gjq_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gjq_db;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar_bucket` VARCHAR(50) COMMENT '头像存储桶',
    `avatar_object_key` VARCHAR(255) COMMENT '头像对象键',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色(0:普通用户 1:管理员)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0:禁用 1:启用)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化管理员用户(初始密码：123456)
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '12345678901', 'admin@example.com', 1, 1);

-- 初始化测试用户(初始密码：123456)
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) 
VALUES ('test', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', '12345678902', 'test@example.com', 0, 1);

-- 增加更多测试用户
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) 
VALUES ('user001', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13800138001', 'zhang@example.com', 0, 1);

INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) 
VALUES ('user002', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13800138002', 'li@example.com', 0, 1);

INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`) 
VALUES ('user003', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13800138003', 'wang@example.com', 0, 1);

-- 类别表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '类别ID',
    `name` VARCHAR(50) NOT NULL COMMENT '类别名称',
    `description` VARCHAR(255) COMMENT '类别描述',
    `icon_bucket` VARCHAR(50) COMMENT '图标存储桶',
    `icon_object_key` VARCHAR(255) COMMENT '图标对象键',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='类别表';

-- 景点表
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '景点ID',
    `title` VARCHAR(100) NOT NULL COMMENT '景点名称',
    `description` TEXT COMMENT '景点描述',
    `category_id` BIGINT NOT NULL COMMENT '类别ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID（创建者）',
    `cover_bucket` VARCHAR(50) COMMENT '封面图片存储桶',
    `cover_object_key` VARCHAR(255) COMMENT '封面图片对象键',
    `file_bucket` VARCHAR(50) COMMENT '文件存储桶',
    `file_object_key` VARCHAR(255) COMMENT '文件对象键',
    `tags` VARCHAR(255) COMMENT '标签，使用逗号分隔',
    `extra_data` TEXT COMMENT '额外数据，JSON格式，用于存储景点特有字段（开放时间、门票价格、地址、交通等）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_category_id` (`category_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='景点表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `item_like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '景点ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '景点ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '景点ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT COMMENT '父评论ID，为空表示顶级评论',
    `reply_to_comment_id` BIGINT COMMENT '被回复的评论ID',
    `reply_to_user_id` BIGINT COMMENT '被回复的用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_item_id` (`item_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 初始化类别数据
INSERT INTO `category` (`name`, `description`) VALUES ('自然风光', '山川、湖泊、森林、草原等自然景观');
INSERT INTO `category` (`name`, `description`) VALUES ('历史文化', '古建筑、博物馆、文化古迹等人文景观');
INSERT INTO `category` (`name`, `description`) VALUES ('主题乐园', '游乐园、动物园、海洋馆等娱乐场所');
INSERT INTO `category` (`name`, `description`) VALUES ('城市地标', '地标建筑、观景台、商业街区等城市景点');
INSERT INTO `category` (`name`, `description`) VALUES ('海滨度假', '海滩、海岛、度假村等海滨旅游');
INSERT INTO `category` (`name`, `description`) VALUES ('温泉养生', '温泉度假村、养生会所、疗养胜地');
INSERT INTO `category` (`name`, `description`) VALUES ('户外探险', '徒步路线、攀岩场地、露营基地等户外活动');
INSERT INTO `category` (`name`, `description`) VALUES ('宗教圣地', '寺庙、教堂、道观等宗教文化场所');
INSERT INTO `category` (`name`, `description`) VALUES ('美食街区', '特色美食街、夜市、美食广场等');
INSERT INTO `category` (`name`, `description`) VALUES ('购物娱乐', '购物中心、娱乐综合体、特色商业区');

-- 初始化景点数据（旅游景点）
-- 自然风光类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('张家界国家森林公园', '世界自然遗产，以奇峰异石著称的自然风景区', 1, 1, '张家界,自然风光,世界遗产,奇峰', '{"ticketPrice":"248元","openTime":"07:00-18:00","address":"湖南省张家界市武陵源区","traffic":"飞机：张家界荷花机场；火车：张家界火车站；汽车：市内多条公交线路","bestSeason":"春秋两季","playTime":"2-3天","features":["奇峰异石","云海奇观","玻璃栈道","索道体验"],"latitude":29.3439,"longitude":110.4793}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('九寨沟风景名胜区', '童话世界般的自然保护区，以彩池、瀑布、雪山著名', 1, 1, '九寨沟,自然风光,彩池,瀑布', '{"ticketPrice":"169元","openTime":"08:00-17:00","address":"四川省阿坝藏族羌族自治州九寨沟县","traffic":"飞机：九寨黄龙机场；汽车：成都茶店子客运站","bestSeason":"秋季（9-11月）","playTime":"2天","features":["五彩池","诺日朗瀑布","原始森林","藏族文化"],"latitude":33.2640,"longitude":103.9240}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('桂林漓江风景区', '山水甲天下的经典山水风光，漓江精华游段', 1, 2, '桂林,漓江,山水,竹筏', '{"ticketPrice":"215元","openTime":"08:00-16:00","address":"广西桂林市","traffic":"飞机：桂林两江国际机场；火车：桂林站、桂林北站","bestSeason":"4-10月","playTime":"1天","features":["竹筏漂流","山水倒影","渔火夜景","摄影胜地"],"latitude":25.2740,"longitude":110.2993}');

-- 历史文化类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('故宫博物院', '明清两朝皇宫，世界最大的古代宫殿建筑群', 2, 1, '故宫,历史文化,皇宫,古建筑', '{"ticketPrice":"60元","openTime":"08:30-17:00","address":"北京市东城区景山前街4号","traffic":"地铁1号线天安门东站；公交1、2、10、20路","bestSeason":"春秋两季","playTime":"半天","features":["古代建筑","文物珍藏","历史文化","皇家园林"],"latitude":39.9163,"longitude":116.3972}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('兵马俑博物馆', '秦始皇兵马俑，世界第八大奇迹', 2, 2, '兵马俑,历史文化,秦朝,文物', '{"ticketPrice":"120元","openTime":"08:30-17:00","address":"陕西省西安市临潼区秦陵北路","traffic":"西安市区乘坐游5路；高铁西安北站转地铁","bestSeason":"春秋两季","playTime":"半天","features":["兵马俑坑","青铜器","历史遗迹","考古发现"],"latitude":34.3844,"longitude":109.2731}');

-- 主题乐园类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('上海迪士尼乐园', '中国大陆首家迪士尼主题乐园', 3, 1, '迪士尼,主题乐园,娱乐,家庭', '{"ticketPrice":"399元","openTime":"09:00-21:00","address":"上海市浦东新区川沙镇黄赵路310号","traffic":"地铁11号线迪士尼站；浦东机场2号线转11号线","bestSeason":"四季皆宜","playTime":"1天","features":["迪士尼城堡","刺激项目","表演秀","主题餐厅"],"latitude":31.1434,"longitude":121.6661}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('北京欢乐谷', '大型主题公园，拥有多个主题区域', 3, 2, '欢乐谷,主题乐园,过山车,娱乐', '{"ticketPrice":"299元","openTime":"09:30-22:00","address":"北京市朝阳区东四环小武基北路","traffic":"地铁7号线欢乐谷景区站；多路公交车","bestSeason":"春夏秋三季","playTime":"1天","features":["过山车","水上项目","夜场表演","儿童乐园"],"latitude":39.8673,"longitude":116.4978}');

-- 城市地标类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('东方明珠塔', '上海标志性建筑，登高观景好去处', 4, 1, '东方明珠,城市地标,观景,上海', '{"ticketPrice":"160元","openTime":"08:00-21:30","address":"上海市浦东新区世纪大道1号","traffic":"地铁2号线陆家嘴站；多路公交到东方明珠站","bestSeason":"四季皆宜","playTime":"2-3小时","features":["观景台","旋转餐厅","上海历史陈列馆","夜景"],"latitude":31.2397,"longitude":121.4994}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('广州塔', '小蛮腰电视塔，广州新地标', 4, 2, '广州塔,城市地标,观景,小蛮腰', '{"ticketPrice":"150元","openTime":"09:00-23:00","address":"广州市海珠区阅江西路222号","traffic":"地铁3号线客村站；APM线海心沙站","bestSeason":"四季皆宜","playTime":"2-3小时","features":["高空观景","摩天轮","空中漫步","夜景灯光"],"latitude":23.1059,"longitude":113.3240}');

-- 海滨度假类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('三亚亚龙湾', '天下第一湾，热带海滨度假胜地', 5, 1, '三亚,海滨度假,沙滩,热带', '{"ticketPrice":"免费","openTime":"全天开放","address":"海南省三亚市吉阳区亚龙湾路","traffic":"三亚机场乘机场巴士；市区乘15、25、27路公交","bestSeason":"10月-4月","playTime":"1-2天","features":["细白沙滩","清澈海水","水上运动","度假酒店"],"latitude":18.2342,"longitude":109.6793}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('青岛金沙滩', '亚洲最大的沙滩，水清沙细', 5, 2, '青岛,海滨度假,金沙滩,海水浴场', '{"ticketPrice":"免费","openTime":"全天开放","address":"山东省青岛市黄岛区","traffic":"地铁1号线金沙滩站；隧道公交3、4路","bestSeason":"5月-10月","playTime":"半天","features":["金色沙滩","海水浴场","沙滩排球","海鲜美食"],"latitude":35.9730,"longitude":119.8540}');

-- 温泉养生类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('安宁温泉', '天然地热温泉，养生度假胜地', 6, 1, '安宁,温泉养生,地热,度假', '{"ticketPrice":"128元","openTime":"08:00-24:00","address":"云南省昆明市安宁市温泉街道","traffic":"昆明南部客运站乘班车；自驾昆明市区40分钟","bestSeason":"四季皆宜","playTime":"半天","features":["天然温泉","养生SPA","温泉酒店","山谷环境"],"latitude":24.9199,"longitude":102.4677}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('华清宫温泉', '历史悠久的皇家温泉，杨贵妃沐浴地', 6, 2, '华清宫,温泉养生,历史,皇家', '{"ticketPrice":"120元","openTime":"07:00-19:00","address":"陕西省西安市临潼区华清路038号","traffic":"西安火车站乘游5路；地铁9号线华清池站","bestSeason":"四季皆宜","playTime":"半天","features":["历史温泉","古建筑","长恨歌演出","园林景观"],"latitude":34.3619,"longitude":109.2144}');

-- 户外探险类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('华山风景区', '奇险天下第一山，登山探险圣地', 7, 1, '华山,户外探险,登山,险峻', '{"ticketPrice":"160元","openTime":"24小时","address":"陕西省渭南市华阴市华山镇","traffic":"西安北站高铁到华山北站；华山北站乘免费班车","bestSeason":"春秋两季","playTime":"1-2天","features":["险峻山峰","日出观景","长空栈道","登山挑战"],"latitude":34.4754,"longitude":110.0856}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('张家界大峡谷玻璃桥', '世界最长玻璃悬索桥，极限体验', 7, 2, '张家界,户外探险,玻璃桥,极限', '{"ticketPrice":"256元","openTime":"07:30-17:30","address":"湖南省张家界市慈利县三官寺乡","traffic":"张家界市区乘班车；自驾约1小时","bestSeason":"春秋两季","playTime":"半天","features":["玻璃桥","蹦极跳","峡谷漂流","高空滑索"],"latitude":29.3276,"longitude":110.2859}');

-- 宗教圣地类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('普陀山风景区', '观音菩萨道场，佛教圣地', 8, 1, '普陀山,宗教圣地,佛教,观音', '{"ticketPrice":"160元","openTime":"06:00-18:00","address":"浙江省舟山市普陀区","traffic":"舟山普陀山机场；宁波、上海有直达班船","bestSeason":"春秋两季","playTime":"1-2天","features":["佛教寺庙","观音圣像","海岛风光","朝拜祈福"],"latitude":29.9819,"longitude":122.3864}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('少林寺', '禅宗祖庭，功夫发源地', 8, 2, '少林寺,宗教圣地,佛教,功夫', '{"ticketPrice":"80元","openTime":"08:00-17:00","address":"河南省郑州市登封市嵩山少林寺","traffic":"郑州客运站乘班车；登封市区乘8路公交","bestSeason":"春秋两季","playTime":"半天","features":["古寺建筑","武术表演","塔林","禅宗文化"],"latitude":34.5089,"longitude":112.9370}');

-- 美食街区类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('成都锦里古街', '三国文化古街，成都美食集中地', 9, 1, '锦里,美食街区,成都,小吃', '{"ticketPrice":"免费","openTime":"全天开放","address":"四川省成都市武侯区武侯祠大街231号","traffic":"地铁3号线高升桥站；1、57、82路公交","bestSeason":"四季皆宜","playTime":"2-3小时","features":["四川小吃","传统工艺","古建筑","夜景"],"latitude":30.6477,"longitude":104.0551}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('西安回民街', '千年古街，西安特色美食街', 9, 2, '回民街,美食街区,西安,清真', '{"ticketPrice":"免费","openTime":"全天开放","address":"陕西省西安市莲湖区北院门","traffic":"地铁2号线钟楼站；4、7、15路公交","bestSeason":"四季皆宜","playTime":"2-3小时","features":["清真美食","陕西小吃","古街建筑","夜市"],"latitude":34.2658,"longitude":108.9398}');

-- 购物娱乐类景点
INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('南京路步行街', '中华商业第一街，购物娱乐天堂', 10, 1, '南京路,购物娱乐,步行街,上海', '{"ticketPrice":"免费","openTime":"全天开放","address":"上海市黄浦区南京东路","traffic":"地铁1、2、8号线人民广场站；多路公交","bestSeason":"四季皆宜","playTime":"半天","features":["品牌购物","老字号","观光小火车","夜景灯光"],"latitude":31.2359,"longitude":121.4737}');

INSERT INTO `item` (`title`, `description`, `category_id`, `user_id`, `tags`, `extra_data`) 
VALUES ('王府井大街', '北京著名商业街，购物美食一条街', 10, 2, '王府井,购物娱乐,商业街,北京', '{"ticketPrice":"免费","openTime":"全天开放","address":"北京市东城区王府井大街","traffic":"地铁1号线王府井站；103、104、420路公交","bestSeason":"四季皆宜","playTime":"半天","features":["购物中心","小吃街","老字号","步行街"],"latitude":39.9098,"longitude":116.4129}');

-- 用户行为表
CREATE TABLE IF NOT EXISTS `user_action` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行为ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '景点ID',
    `action_type` TINYINT NOT NULL COMMENT '行为类型(0:浏览 1:预约)',
    `extra_data` TEXT COMMENT '额外数据，JSON格式',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_action_type` (`action_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为表';

-- 初始化用户行为数据（旅游用户行为）
-- 浏览行为（action_type=0）：extra_data只存储浏览时间
INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 1, 0, '{"viewTime":"2024-01-15T08:30:00.000Z","source":"浏览器"}', '2024-01-15 16:30:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 2, 0, '{"viewTime":"2024-01-15T09:15:00.000Z","source":"浏览器"}', '2024-01-15 17:15:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 3, 0, '{"viewTime":"2024-01-15T10:20:00.000Z","source":"浏览器"}', '2024-01-15 18:20:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 4, 0, '{"viewTime":"2024-01-15T14:30:00.000Z","source":"浏览器"}', '2024-01-15 22:30:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 5, 0, '{"viewTime":"2024-01-16T09:45:00.000Z","source":"浏览器"}', '2024-01-16 17:45:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 1, 0, '{"viewTime":"2024-01-16T10:15:00.000Z","source":"浏览器"}', '2024-01-16 18:15:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 6, 0, '{"viewTime":"2024-01-16T11:20:00.000Z","source":"浏览器"}', '2024-01-16 19:20:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 7, 0, '{"viewTime":"2024-01-16T15:30:00.000Z","source":"浏览器"}', '2024-01-16 23:30:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 8, 0, '{"viewTime":"2024-01-17T08:45:00.000Z","source":"浏览器"}', '2024-01-17 16:45:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 9, 0, '{"viewTime":"2024-01-17T13:15:00.000Z","source":"浏览器"}', '2024-01-17 21:15:00');

-- 预约行为（action_type=1）：extra_data存储预约相关信息
INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 1, 1, '{"reservationId":"RES202401150001","adults":2,"children":0,"totalPrice":496,"paymentMethod":"支付宝","travelDate":"2024-02-10","contactInfo":"张先生 13800138001"}', '2024-01-15 16:45:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 4, 1, '{"reservationId":"RES202401150002","adults":1,"children":0,"totalPrice":450,"paymentMethod":"微信支付","travelDate":"2024-02-15","contactInfo":"张先生 13800138001"}', '2024-01-15 23:15:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 5, 1, '{"reservationId":"RES202401160001","adults":2,"children":1,"totalPrice":2160,"paymentMethod":"信用卡","travelDate":"2024-03-01","contactInfo":"李女士 13800138002"}', '2024-01-16 19:30:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 7, 1, '{"reservationId":"RES202401160002","adults":2,"children":0,"totalPrice":400,"paymentMethod":"支付宝","travelDate":"2024-03-05","contactInfo":"李女士 13800138002"}', '2024-01-16 23:45:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 10, 1, '{"reservationId":"RES202401170001","adults":3,"children":0,"totalPrice":555,"paymentMethod":"支付宝","travelDate":"2024-03-10","contactInfo":"张先生 13800138001"}', '2024-01-17 22:00:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 12, 1, '{"reservationId":"RES202401180001","adults":1,"children":0,"totalPrice":268,"paymentMethod":"微信支付","travelDate":"2024-03-20","contactInfo":"李女士 13800138002"}', '2024-01-18 15:30:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 13, 1, '{"reservationId":"RES202401180002","adults":2,"children":1,"totalPrice":450,"paymentMethod":"支付宝","travelDate":"2024-04-01","contactInfo":"张先生 13800138001"}', '2024-01-18 20:15:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 16, 1, '{"reservationId":"RES202401190001","adults":2,"children":0,"totalPrice":298,"paymentMethod":"支付宝","travelDate":"2024-04-05","contactInfo":"李女士 13800138002"}', '2024-01-19 14:20:00');

-- 增加更多浏览行为数据以丰富统计
INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 11, 0, '{"viewTime":"2024-01-18T09:30:00.000Z","source":"浏览器"}', '2024-01-18 17:30:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 12, 0, '{"viewTime":"2024-01-18T10:15:00.000Z","source":"浏览器"}', '2024-01-18 18:15:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 13, 0, '{"viewTime":"2024-01-18T19:45:00.000Z","source":"浏览器"}', '2024-01-19 03:45:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 14, 0, '{"viewTime":"2024-01-18T15:20:00.000Z","source":"浏览器"}', '2024-01-18 23:20:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 15, 0, '{"viewTime":"2024-01-18T16:30:00.000Z","source":"浏览器"}', '2024-01-19 00:30:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 16, 0, '{"viewTime":"2024-01-19T13:15:00.000Z","source":"浏览器"}', '2024-01-19 21:15:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 17, 0, '{"viewTime":"2024-01-19T11:40:00.000Z","source":"浏览器"}', '2024-01-19 19:40:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (1, 18, 0, '{"viewTime":"2024-01-19T14:25:00.000Z","source":"浏览器"}', '2024-01-19 22:25:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 19, 0, '{"viewTime":"2024-01-19T16:50:00.000Z","source":"浏览器"}', '2024-01-20 00:50:00');

INSERT INTO `user_action` (`user_id`, `item_id`, `action_type`, `extra_data`, `create_time`) 
VALUES (2, 20, 0, '{"viewTime":"2024-01-20T08:30:00.000Z","source":"浏览器"}', '2024-01-20 16:30:00');

-- 初始化收藏数据
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (1, 1, '2024-01-15 16:35:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (1, 3, '2024-01-15 18:25:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (1, 8, '2024-01-17 17:00:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (1, 11, '2024-01-18 17:35:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (2, 2, '2024-01-16 18:20:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (2, 5, '2024-01-16 19:35:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (2, 9, '2024-01-17 16:50:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (2, 14, '2024-01-18 23:25:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (3, 1, '2024-01-19 10:15:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (3, 6, '2024-01-19 14:30:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (4, 4, '2024-01-19 16:45:00');
INSERT INTO `favorite` (`user_id`, `item_id`, `create_time`) VALUES (4, 12, '2024-01-20 09:20:00');

-- 初始化点赞数据
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (1, 1, '2024-01-15 16:32:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (1, 2, '2024-01-15 17:18:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (1, 5, '2024-01-16 17:48:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (1, 8, '2024-01-17 16:48:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (1, 11, '2024-01-18 17:33:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (2, 1, '2024-01-16 18:18:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (2, 3, '2024-01-16 19:25:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (2, 7, '2024-01-16 23:33:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (2, 9, '2024-01-17 21:18:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (2, 16, '2024-01-19 21:18:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (3, 2, '2024-01-19 11:30:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (3, 6, '2024-01-19 15:20:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (3, 10, '2024-01-19 18:40:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (4, 4, '2024-01-19 17:15:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (4, 12, '2024-01-20 09:25:00');
INSERT INTO `item_like` (`user_id`, `item_id`, `create_time`) VALUES (4, 15, '2024-01-20 14:10:00');

-- 初始化评论数据
INSERT INTO `comment` (`user_id`, `item_id`, `content`, `create_time`) VALUES (1, 1, '张家界的风景真的太美了！奇峰异石令人震撼，玻璃栈道体验惊险刺激，强烈推荐！', '2024-01-15 17:00:00');
INSERT INTO `comment` (`user_id`, `item_id`, `content`, `create_time`) VALUES (2, 1, '张家界门票确实有点贵，但是景色绝对值得，建议至少安排2天时间游玩', '2024-01-16 19:00:00');
INSERT INTO `comment` (`user_id`, `item_id`, `content`, `create_time`) VALUES (1, 4, '故宫真的是历史文化的瑰宝，每一处建筑都充满故事，导览讲解非常专业', '2024-01-16 08:30:00');
INSERT INTO `comment` (`user_id`, `item_id`, `content`, `create_time`) VALUES (2, 5, '上海迪士尼乐园的项目很刺激，小朋友玩得非常开心，就是排队时间较长', '2024-01-16 20:15:00');
INSERT INTO `comment` (`user_id`, `item_id`, `content`, `create_time`) VALUES (3, 1, '朋友推荐来的张家界，确实没有失望，空气清新风景如画，拍照效果很好', '2024-01-19 11:45:00');
INSERT INTO `comment` (`user_id`, `item_id`, `content`, `create_time`) VALUES (3, 6, '东方明珠塔的观景台视野开阔，可以俯瞰整个上海，夜景特别美', '2024-01-19 16:20:00');
INSERT INTO `comment` (`user_id`, `item_id`, `content`, `create_time`) VALUES (4, 12, '成都锦里古街的小吃真的太丰富了，孩子们尝遍了各种美食，非常满足', '2024-01-20 10:30:00');

-- 聊天会话表
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `session_name` VARCHAR(100) NOT NULL COMMENT '会话名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    `session_id` BIGINT NOT NULL COMMENT '会话ID',
    `role` VARCHAR(20) NOT NULL COMMENT '角色(user/assistant)',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `model` VARCHAR(50) COMMENT '使用的模型名称',
    `extra_data` TEXT COMMENT '额外数据，JSON格式，用于存储文件信息等',
    `message_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间',
    KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 初始化聊天会话数据（旅游场景）
INSERT INTO `chat_session` (`user_id`, `session_name`) 
VALUES (1, '景点咨询与推荐');

INSERT INTO `chat_session` (`user_id`, `session_name`) 
VALUES (2, '旅行计划规划');

INSERT INTO `chat_session` (`user_id`, `session_name`) 
VALUES (3, '预约服务咨询');

INSERT INTO `chat_session` (`user_id`, `session_name`) 
VALUES (4, '旅游优惠了解');

-- 初始化聊天消息数据（旅游推荐场景）
-- 会话1：景点咨询与推荐
INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (1, 'user', '你好，我想找一些适合家庭出游的景点，最好是自然风光类的，有什么推荐吗？', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (1, 'assistant', '您好！为您推荐几个非常适合家庭出游的自然风光景点：\n\n🏔️ **张家界国家森林公园** - 门票248元\n- 特色：奇峰异石、玻璃栈道、索道体验\n- 适合：老少皆宜，有无障碍设施\n- 游玩时间：2-3天\n\n🌊 **桂林漓江风景区** - 门票215元\n- 特色：山水甲天下、竹筏漂流、摄影胜地\n- 适合：全家人乘船游览，轻松舒适\n- 游玩时间：1天\n\n💎 **九寨沟风景名胜区** - 门票169元\n- 特色：五彩池、瀑布群、原始森林\n- 适合：拍照打卡，孩子会很喜欢\n- 游玩时间：2天\n\n您比较倾向于哪种类型的自然风光呢？山峰、水景还是森林？我可以为您详细介绍～', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (1, 'user', '我们想去看山峰和奇石，张家界听起来不错，能详细介绍一下吗？', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (1, 'assistant', '张家界绝对是看山峰奇石的绝佳选择！为您详细介绍：\n\n🎫 **门票信息**：\n- 成人票：248元（4天有效）\n- 学生/老人：163元\n- 1.3米以下儿童免费\n\n🏔️ **必看景点**：\n- **袁家界**：阿凡达取景地，悬浮山原型\n- **天子山**：云海日出，视野开阔\n- **黄石寨**：张家界第一个观景台\n- **金鞭溪**：平缓步道，适合全家漫步\n\n🚡 **交通方式**：\n- 天子山索道：往返67元，8分钟到山顶\n- 百龙天梯：往返72元，世界最高户外电梯\n- 徒步登山：免费但较累，约2-3小时\n\n✨ **家庭友好设施**：\n- 景区内有观光车，老人孩子不用担心体力\n- 多个休息点和餐厅\n- 安全防护措施完善\n\n**最佳游览路线**：第一天金鞭溪+袁家界，第二天天子山+黄石寨\n\n需要我帮您查询最近的住宿和交通信息吗？', 'qwen-turbo');

-- 会话2：旅行计划规划
INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (2, 'user', '我想规划一个5天的华东旅游行程，想去上海、杭州和苏州，求推荐路线', 'qwen-plus');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (2, 'assistant', '华东三城5天游是经典路线！为您规划如下行程：\n\n📅 **Day 1-2：上海（2天）**\n🌃 **第一天**：\n- 上午：外滩 → 南京路步行街\n- 下午：东方明珠塔 → 陆家嘴金融区\n- 晚上：黄浦江夜游\n\n🏙️ **第二天**：\n- 上午：上海迪士尼乐园\n- 下午：继续迪士尼\n- 晚上：迪士尼小镇\n\n📅 **Day 3-4：杭州（2天）**\n🌸 **第三天**：\n- 上午：西湖（断桥残雪、苏堤春晓）\n- 下午：雷峰塔 → 三潭印月\n- 晚上：湖滨路夜景\n\n🍃 **第四天**：\n- 上午：灵隐寺 → 飞来峰\n- 下午：河坊街古街 → 南宋御街\n- 晚上：西湖音乐喷泉\n\n📅 **Day 5：苏州（1天）**\n🏛️ **第五天**：\n- 上午：拙政园 → 苏州博物馆\n- 下午：平江路古街 → 观前街\n- 晚间：返程\n\n🚄 **交通建议**：高铁连接，上海↔杭州1小时，杭州↔苏州1.5小时\n\n这个安排怎么样？需要调整哪些地方吗？', 'qwen-plus');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (2, 'user', '行程很棒！能帮我估算一下费用吗？我们是2个成人1个小孩', 'qwen-plus');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (2, 'assistant', '好的！为您详细计算三人5天行程的费用：\n\n🚄 **交通费用**：\n- 高铁票：上海↔杭州↔苏州 = 约600元/人 × 2 = 1200元\n- 儿童高铁票：约300元\n- 市内交通（地铁、公交、打车）：约400元\n- **交通小计：1900元**\n\n🎫 **门票费用**：\n- 东方明珠塔：160元×2 + 80元×1 = 400元\n- 上海迪士尼：399元×2 + 299元×1 = 1097元\n- 西湖游船：55元×2 + 35元×1 = 145元\n- 雷峰塔：40元×2 + 20元×1 = 100元\n- 拙政园：90元×2 + 45元×1 = 225元\n- **门票小计：1967元**\n\n🏨 **住宿费用**（4晚）：\n- 上海（2晚）：300元/晚 × 2 = 600元\n- 杭州（2晚）：250元/晚 × 2 = 500元\n- **住宿小计：1100元**\n\n🍽️ **餐饮费用**：\n- 每日餐费约200元 × 5天 = 1000元\n\n💰 **总计费用：约6000元**\n\n💡 **省钱贴士**：\n- 提前订票可享早鸟优惠\n- 部分景点有套票优惠\n- 选择民宿可节省住宿费\n\n需要我推荐一些性价比高的酒店吗？', 'qwen-plus');

-- 会话3：预约服务咨询
INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (3, 'user', '我想预约三亚亚龙湾的行程，包括酒店和门票，怎么预约比较划算？', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (3, 'assistant', '三亚亚龙湾是度假天堂！为您介绍预约攻略：\n\n🏖️ **亚龙湾景区信息**：\n- 门票：免费开放\n- 开放时间：全天\n- 最佳季节：10月-4月\n\n🏨 **酒店预约建议**：\n\n⭐ **豪华型**：\n- 亚龙湾丽思卡尔顿：1800-3000元/晚\n- 亚龙湾红树林：1200-2000元/晚\n\n⭐ **舒适型**：\n- 亚龙湾环球城：600-1000元/晚\n- 亚龙湾美高梅：800-1200元/晚\n\n💰 **预约优惠渠道**：\n1. **官方网站**：直接预约，常有会员价\n2. **OTA平台**：携程、美团、飞猪对比价格\n3. **套餐预约**：机+酒套餐通常更优惠\n4. **旅行社**：可定制化服务\n\n🎁 **套餐推荐**：\n- 4天3晚自由行：含往返机票+酒店+接送 = 2500-4000元/人\n- 酒店直通车：含酒店+机场接送+部分餐食\n\n您计划什么时候去？几天行程？我可以帮您查找具体的优惠套餐！', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (3, 'user', '计划4月份去，4天3晚，2个大人，想住海景房', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (3, 'assistant', '4月份是三亚的黄金季节！为您推荐海景房套餐：\n\n🌊 **海景房酒店推荐**：\n\n🏨 **亚龙湾红树林度假酒店**（推荐）\n- 海景房：1500-1800元/晚\n- 包含：自助早餐、无边泳池、私家海滩\n- 特色：亲子友好、设施完善\n\n🏨 **亚龙湾美高梅度假酒店**\n- 海景房：1200-1500元/晚\n- 包含：自助早餐、海滩俱乐部\n- 特色：性价比高、位置绝佳\n\n💰 **4月份套餐价格**（2人4天3晚）：\n\n📦 **红树林套餐**：\n- 往返机票：1600元×2 = 3200元\n- 海景房3晚：1650元×3 = 4950元\n- 机场接送：200元\n- **总计：8350元**\n\n📦 **美高梅套餐**：\n- 往返机票：1600元×2 = 3200元\n- 海景房3晚：1350元×3 = 4050元\n- 机场接送：200元\n- **总计：7450元**\n\n🎯 **预约建议**：\n- 提前30天预约享早鸟8.5折\n- 工作日入住比周末便宜300-500元\n- 建议选择包含早餐的套餐\n\n需要我帮您对比更多酒店选项吗？', 'qwen-turbo');

-- 会话4：旅游优惠了解
INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (4, 'user', '最近有什么旅游优惠活动吗？我想带家人出去玩', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (4, 'assistant', '现在正值春季旅游旺季，有很多超值优惠活动：\n\n🎉 **平台大促活动**：\n- 春游季大促：满2000减200，满5000减600\n- 新用户专享：首次预约立减300元\n- 家庭套餐：3人以上享9折优惠\n\n🌸 **热门目的地优惠**：\n\n🏔️ **山岳风光类**：\n- 张家界门票：248元 → 199元（限时8折）\n- 华山+住宿套餐：原价899元 → 699元\n- 黄山3日游：含门票+住宿+导游 = 特价599元\n\n🌊 **海滨度假类**：\n- 三亚4天3晚：机票+酒店 = 立减800元\n- 青岛海滨2日游：高铁+酒店 = 399元起\n- 厦门鼓浪屿：门票+船票套餐 = 8折优惠\n\n🎡 **亲子游特惠**：\n- 上海迪士尼：成人+儿童套票 = 省150元\n- 北京欢乐谷：家庭套票买2送1\n- 长隆野生动物园：亲子套票7折\n\n🎁 **增值服务**：\n- 免费接送机服务\n- 赠送旅游保险\n- 24小时客服支持\n\n您比较想去哪个方向呢？我可以为您推荐最合适的优惠套餐！', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (4, 'user', '我们想去亲子游，孩子6岁，有什么推荐的优惠套餐吗？', 'qwen-turbo');

INSERT INTO `chat_message` (`session_id`, `role`, `content`, `model`) 
VALUES (4, 'assistant', '6岁的孩子正是最适合亲子游的年龄！为您推荐几个超值套餐：\n\n🎢 **主题乐园套餐**：\n\n🏰 **上海迪士尼2日套餐**：\n- 包含：2日门票+1晚主题酒店+早餐\n- 原价：2800元/家庭（2大1小）\n- **活动价：2299元，省501元**\n- 赠送：快速通行证+纪念品\n\n🎡 **北京欢乐谷+野生动物园**：\n- 包含：两园门票+北京2晚住宿+早餐\n- 原价：1899元\n- **活动价：1499元，省400元**\n- 特色：动物园+游乐园组合，寓教于乐\n\n🐧 **长隆度假区3日游**：\n- 包含：野生动物园+欢乐世界+马戏+2晚酒店\n- 原价：3200元\n- **活动价：2680元，省520元**\n- 亮点：孩子最爱的动物主题\n\n🌊 **海滨亲子游**：\n\n🏖️ **青岛亲子3日游**：\n- 包含：海底世界+栈桥+金沙滩+住宿+餐食\n- 特价：1299元/家庭\n- 特色：看海豚表演，玩沙滩，孩子超喜欢\n\n💡 **特别优惠**：\n- 6岁儿童享受儿童价（通常是成人价6-7折）\n- 现在预约送儿童专属大礼包\n- 可免费取消，出行前3天无损退款\n\n您比较倾向于哪种类型的亲子游呢？', 'qwen-turbo'); 