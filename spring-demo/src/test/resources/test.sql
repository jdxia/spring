-- 测试sql

-- 创建 mybatis 数据库
CREATE DATABASE mybatis;

-- 具体sql
CREATE TABLE `tbl_department` (
                                  `id` varchar(32) NOT NULL,
                                  `name` varchar(32) NOT NULL,
                                  `tel` varchar(18) DEFAULT NULL,
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tbl_user` (
                            `id` varchar(32) NOT NULL,
                            `version` int NOT NULL DEFAULT '0',
                            `name` varchar(32) NOT NULL,
                            `age` int DEFAULT NULL,
                            `birthday` datetime DEFAULT NULL,
                            `department_id` varchar(32) NOT NULL,
                            `sorder` int NOT NULL DEFAULT '1',
                            `deleted` tinyint(1) NOT NULL DEFAULT '0',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `tbl_user` (`id`, `version`, `name`, `age`, `birthday`, `department_id`, `sorder`, `deleted`)
VALUES
    ('09ec5fcea620c168936deee53a9cdcfb', 0, '阿熊', 18, '2003-08-08 10:00:00', '18ec781fbefd727923b0d35740b177ab', 1, 0),
    ('5d0eebc4f370f3bd959a4f7bc2456d89', 0, '老狗', 30, '1991-02-20 15:27:20', 'ee0e342201004c1721e69a99ac0dc0df', 1, 0);


INSERT INTO `tbl_department` (`id`, `name`, `tel`)
VALUES
    ('00000000000000000000000000000000', '全部部门', '-'),
    ('18ec781fbefd727923b0d35740b177ab', '开发部', '123'),
    ('53e3803ebbf4f97968e0253e5ad4cc83', '测试产品部', '789'),
    ('ee0e342201004c1721e69a99ac0dc0df', '运维部', '456');

