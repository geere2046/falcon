-----------------------------------------------
-- Export file for user XCLBS                --
-- Created by huangyc on 2016/5/16, 15:26:19 --
-----------------------------------------------

spool createTable.log

prompt
prompt Creating table MAP_POI
prompt ======================
prompt
create table MAP_POI
(
  ID             CHAR(32) not null,
  COMPANY_ID     CHAR(32) not null,
  ICON_ID        CHAR(32) not null,
  POI_NAME       VARCHAR2(100) not null,
  LATITUDE       NUMBER(13,10) not null,
  LONGITUDE      NUMBER(13,10) not null,
  OPER_UID       CHAR(32) not null,
  CREATE_TIME    DATE not null,
  DEPARTMENT_ID  CHAR(32),
  POI_IMAGE      VARCHAR2(300),
  NOTE           VARCHAR2(600),
  SHARE_TYPE     CHAR(2) not null,
  POI_ZOOM_LEVEL NUMBER(3),
  AREA_CODE      VARCHAR2(32),
  MAP_AREA_TYPE  VARCHAR2(1),
  MAP_TYPE       VARCHAR2(1),
  DISPLAY_FLAG   VARCHAR2(2),
  APPLY_FUNC     VARCHAR2(2),
  PPOI_ID        CHAR(32)
)
tablespace XCLBS
  pctfree 10
  initrans 1
  maxtrans 255;
comment on table MAP_POI
  is '地图区域信息表（网格信息表）';
comment on column MAP_POI.ICON_ID
  is '与兴趣点图标信息表关联';
comment on column MAP_POI.POI_IMAGE
  is '兴趣点所要展示的图片   ';
comment on column MAP_POI.NOTE
  is '备注描述 ';
comment on column MAP_POI.SHARE_TYPE
  is '01:系统级共享02：公司级共享03：客户自定义共享';
comment on column MAP_POI.MAP_AREA_TYPE
  is '1：圆 2：矩形 3：多边形 空或者4为兴趣点';
comment on column MAP_POI.MAP_TYPE
  is '1:bmap 2:google map 3:map abc';
comment on column MAP_POI.DISPLAY_FLAG
  is '是否可见(1:是;2:否)';
comment on column MAP_POI.APPLY_FUNC
  is '应用于功能，0表示网格化平台，20表示考勤有效范围功能';
comment on column MAP_POI.PPOI_ID
  is '上级网格ID，上下级隶属关系结构。
顶级网络为空';
alter table MAP_POI
  add constraint PK_MAP_POI primary key (ID)
  using index 
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;

prompt
prompt Creating table MAP_POI_DATA
prompt ===========================
prompt
create table MAP_POI_DATA
(
  ID           CHAR(32) not null,
  POI_ID       CHAR(32) not null,
  CONNECT_FLAG CHAR(32) not null,
  LATITUDE     NUMBER(13,10) not null,
  LONGITUDE    NUMBER(13,10) not null,
  RADIUS       NUMBER(6),
  POINT_ORDER  NUMBER
)
tablespace XCLBS
  pctfree 10
  initrans 1
  maxtrans 255;
comment on table MAP_POI_DATA
  is '兴趣点详细信息，坐标组';
alter table MAP_POI_DATA
  add constraint PK_MAP_POI_DATA primary key (ID)
  using index 
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;

prompt
prompt Creating table WL_INFO
prompt ======================
prompt
create table WL_INFO
(
  ID            CHAR(32) not null,
  WL_NAME       VARCHAR2(300),
  FREQUENCY     NUMBER(6),
  SUB_FREQUENCY NUMBER(6),
  START_TIME    DATE,
  END_TIME      DATE,
  START_DATE    DATE,
  END_DATE      DATE,
  WEEK_DAY      VARCHAR2(7),
  STATUS        VARCHAR2(1),
  CREATE_TIME   DATE,
  WARN_STYLE    VARCHAR2(3),
  RECEIVE_TELS  VARCHAR2(3000),
  CRITERIA      VARCHAR2(1),
  DURATION      NUMBER(6),
  CFCOUNT       NUMBER(6),
  COM_ID        CHAR(32),
  USER_ID       CHAR(32),
  IS_UP_POWER   VARCHAR2(1)
)
tablespace XCLBS
  pctfree 10
  initrans 1
  maxtrans 255;
comment on table WL_INFO
  is '围栏规则设置信息表';
comment on column WL_INFO.WL_NAME
  is '围栏名称';
comment on column WL_INFO.FREQUENCY
  is '频率,单位为分钟';
comment on column WL_INFO.SUB_FREQUENCY
  is '触发告警条件后的频率';
comment on column WL_INFO.START_TIME
  is '开始时间';
comment on column WL_INFO.END_TIME
  is '结束时间';
comment on column WL_INFO.START_DATE
  is '开始日期';
comment on column WL_INFO.END_DATE
  is '结束日期';
comment on column WL_INFO.WEEK_DAY
  is '星期周期，1表示有效，0表示无效。7位字符串分别表示星期天到星期六围栏是否有效的标识位';
comment on column WL_INFO.STATUS
  is '0表示暂失效，1表示生效';
comment on column WL_INFO.CREATE_TIME
  is '创建时间';
comment on column WL_INFO.WARN_STYLE
  is '告警方式由3位组成，
第一位标识告警发送方式：1：短信2：邮件
第二位 标识是否记录停留时长；1表示记录，0表示不记录
第三位标识是否记录出入次数；1表示记录，0表示不记录';
comment on column WL_INFO.RECEIVE_TELS
  is '接警号码';
comment on column WL_INFO.CRITERIA
  is '0离开，1进入，2进出';
comment on column WL_INFO.DURATION
  is '告警延后发出时长，如进入某围栏后，10分钟仍不离开，即告警。
立即告警此值为0；';
comment on column WL_INFO.CFCOUNT
  is '告警条件触发多少次以后才发出告警，默认为1，表示触发即告警。';
comment on column WL_INFO.COM_ID
  is '企业ID';
comment on column WL_INFO.USER_ID
  is 'USERID';
comment on column WL_INFO.IS_UP_POWER
  is 'T表示上报电量，F表示不上报电量';
alter table WL_INFO
  add constraint PK_WL_INFO primary key (ID)
  using index 
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;

prompt
prompt Creating table WL_POI_REL
prompt =========================
prompt
create table WL_POI_REL
(
  POI_ID VARCHAR2(32) not null,
  WL_ID  CHAR(32) not null,
  COM_ID CHAR(32) not null
)
tablespace XCLBS
  pctfree 10
  initrans 1
  maxtrans 255;
comment on table WL_POI_REL
  is '围栏与区域关联关系，关联MAP_POI表和WL_INFO表';
comment on column WL_POI_REL.POI_ID
  is '人员ID/部门ID';
comment on column WL_POI_REL.WL_ID
  is '班次ID';
comment on column WL_POI_REL.COM_ID
  is '企业ID';
alter table WL_POI_REL
  add constraint PK_WL_POI_REL primary key (POI_ID, WL_ID)
  using index 
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;

prompt
prompt Creating table WL_USER_REL
prompt ==========================
prompt
create table WL_USER_REL
(
  COM_ID    CHAR(32) not null,
  USER_TYPE INTEGER,
  USER_ID   CHAR(32) not null,
  WL_ID     CHAR(32) not null
)
tablespace XCLBS
  pctfree 10
  initrans 1
  maxtrans 255;
comment on table WL_USER_REL
  is '围栏适用人员信息表';
comment on column WL_USER_REL.COM_ID
  is '企业ID';
comment on column WL_USER_REL.USER_TYPE
  is '0表示人员，1表示部门';
comment on column WL_USER_REL.USER_ID
  is '人员ID/部门ID';
comment on column WL_USER_REL.WL_ID
  is '班次ID';
alter table WL_USER_REL
  add constraint PK_WL_USER_REL primary key (WL_ID, USER_ID)
  using index 
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;

prompt
prompt Creating table WL_WARN_RECORD
prompt =============================
prompt
create table WL_WARN_RECORD
(
  ID           CHAR(32) not null,
  COM_ID       CHAR(32),
  USER_ID      CHAR(32),
  WL_ID        CHAR(32),
  WARN_STYLE   CHAR(1),
  RECEIVE_TELS VARCHAR2(3000),
  WARN_CONTENT VARCHAR2(3000),
  CRITERIA     VARCHAR2(1),
  LATITUDE     NUMBER(13,10),
  LONGITUDE    NUMBER(13,10),
  LOC_DESC     VARCHAR2(3000),
  CREATE_DATE  DATE,
  DURATION     NUMBER(6),
  CFCOUNT      NUMBER(6)
)
tablespace XCLBS
  pctfree 10
  initrans 1
  maxtrans 255;
comment on table WL_WARN_RECORD
  is '围栏告警记录信息表';
comment on column WL_WARN_RECORD.USER_ID
  is 'USER_ID';
comment on column WL_WARN_RECORD.WL_ID
  is '围栏ID';
comment on column WL_WARN_RECORD.WARN_STYLE
  is '1、表示告警；并触 发花怎么卖
2、记录时长
3、记录触发次数';
comment on column WL_WARN_RECORD.RECEIVE_TELS
  is '接警号码';
comment on column WL_WARN_RECORD.WARN_CONTENT
  is '告警内容';
comment on column WL_WARN_RECORD.CRITERIA
  is '0表示离开，1表示进入，2表示进出';
comment on column WL_WARN_RECORD.LATITUDE
  is '纬度,告警当时的纬度';
comment on column WL_WARN_RECORD.LONGITUDE
  is '经度';
comment on column WL_WARN_RECORD.LOC_DESC
  is '位置描述';
comment on column WL_WARN_RECORD.CREATE_DATE
  is '创建时间/记录时间';
comment on column WL_WARN_RECORD.DURATION
  is '触发告警条件后持续了多长时间,以分钟为单位';
comment on column WL_WARN_RECORD.CFCOUNT
  is '触发次数';
alter table WL_WARN_RECORD
  add constraint PK_WL_WARN_RECORD primary key (ID)
  using index 
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;
create index IDX_WL_WARN_RECORD_USERID on WL_WARN_RECORD (USER_ID)
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;
create index IDX_WL_WARN_RECORD_WLID on WL_WARN_RECORD (WL_ID)
  tablespace XCLBS
  pctfree 10
  initrans 2
  maxtrans 255;


spool off
