create table QRTZ_BLOB_TRIGGERS
(
	SCHED_NAME varchar(120) not null,
	TRIGGER_NAME varchar(200) not null,
TRIGGER_GROUP varchar(200) not null,
BLOB_DATA blob null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)
;

create index SCHED_NAME
	on QRTZ_BLOB_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
;

create table QRTZ_CALENDARS
(
	SCHED_NAME varchar(120) not null,
	CALENDAR_NAME varchar(200) not null,
CALENDAR blob not null,
primary key (SCHED_NAME, CALENDAR_NAME)
)
;

create table QRTZ_CRON_TRIGGERS
(
	SCHED_NAME varchar(120) not null,
	TRIGGER_NAME varchar(200) not null,
TRIGGER_GROUP varchar(200) not null,
CRON_EXPRESSION varchar(120) not null,
TIME_ZONE_ID varchar(80) null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)
;

create table QRTZ_FIRED_TRIGGERS
(
	SCHED_NAME varchar(120) not null,
	ENTRY_ID varchar(95) not null,
TRIGGER_NAME varchar(200) not null,
TRIGGER_GROUP varchar(200) not null,
INSTANCE_NAME varchar(200) not null,
FIRED_TIME bigint(13) not null,
SCHED_TIME bigint(13) not null,
PRIORITY int not null,
STATE varchar(16) not null,
JOB_NAME varchar(200) null,
JOB_GROUP varchar(200) null,
IS_NONCONCURRENT varchar(1) null,
REQUESTS_RECOVERY varchar(1) null,
primary key (SCHED_NAME, ENTRY_ID)
)
;

create index IDX_QRTZ_FT_INST_JOB_REQ_RCVRY
	on QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME, REQUESTS_RECOVERY)
;

create index IDX_QRTZ_FT_JG
	on QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_GROUP)
;

create index IDX_QRTZ_FT_J_G
	on QRTZ_FIRED_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP)
;

create index IDX_QRTZ_FT_TG
	on QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_GROUP)
;

create index IDX_QRTZ_FT_TRIG_INST_NAME
	on QRTZ_FIRED_TRIGGERS (SCHED_NAME, INSTANCE_NAME)
;

create index IDX_QRTZ_FT_T_G
	on QRTZ_FIRED_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
;

create table QRTZ_JOB_DETAILS
(
	SCHED_NAME varchar(120) not null,
	JOB_NAME varchar(200) not null,
JOB_GROUP varchar(200) not null,
DESCRIPTION varchar(250) null,
JOB_CLASS_NAME varchar(250) not null,
IS_DURABLE varchar(1) not null,
IS_NONCONCURRENT varchar(1) not null,
IS_UPDATE_DATA varchar(1) not null,
REQUESTS_RECOVERY varchar(1) not null,
JOB_DATA blob null,
primary key (SCHED_NAME, JOB_NAME, JOB_GROUP)
)
;

create index IDX_QRTZ_J_GRP
	on QRTZ_JOB_DETAILS (SCHED_NAME, JOB_GROUP)
;

create index IDX_QRTZ_J_REQ_RECOVERY
	on QRTZ_JOB_DETAILS (SCHED_NAME, REQUESTS_RECOVERY)
;

create table QRTZ_LOCKS
(
	SCHED_NAME varchar(120) not null,
	LOCK_NAME varchar(40) not null,
primary key (SCHED_NAME, LOCK_NAME)
)
;

create table QRTZ_PAUSED_TRIGGER_GRPS
(
	SCHED_NAME varchar(120) not null,
	TRIGGER_GROUP varchar(200) not null,
primary key (SCHED_NAME, TRIGGER_GROUP)
)
;

create table QRTZ_SCHEDULER_STATE
(
	SCHED_NAME varchar(120) not null,
	INSTANCE_NAME varchar(200) not null,
LAST_CHECKIN_TIME bigint(13) not null,
CHECKIN_INTERVAL bigint(13) not null,
primary key (SCHED_NAME, INSTANCE_NAME)
)
;

create table QRTZ_SIMPLE_TRIGGERS
(
	SCHED_NAME varchar(120) not null,
	TRIGGER_NAME varchar(200) not null,
TRIGGER_GROUP varchar(200) not null,
REPEAT_COUNT bigint(7) not null,
REPEAT_INTERVAL bigint(12) not null,
TIMES_TRIGGERED bigint(10) not null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)
;

create table QRTZ_SIMPROP_TRIGGERS
(
	SCHED_NAME varchar(120) not null,
	TRIGGER_NAME varchar(200) not null,
TRIGGER_GROUP varchar(200) not null,
STR_PROP_1 varchar(512) null,
STR_PROP_2 varchar(512) null,
STR_PROP_3 varchar(512) null,
INT_PROP_1 int null,
INT_PROP_2 int null,
LONG_PROP_1 bigint null,
LONG_PROP_2 bigint null,
DEC_PROP_1 decimal(13,4) null,
DEC_PROP_2 decimal(13,4) null,
BOOL_PROP_1 varchar(1) null,
BOOL_PROP_2 varchar(1) null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
)
;

create table QRTZ_TRIGGERS
(
	SCHED_NAME varchar(120) not null,
	TRIGGER_NAME varchar(200) not null,
TRIGGER_GROUP varchar(200) not null,
JOB_NAME varchar(200) not null,
JOB_GROUP varchar(200) not null,
DESCRIPTION varchar(250) null,
NEXT_FIRE_TIME bigint(13) null,
PREV_FIRE_TIME bigint(13) null,
PRIORITY int null,
TRIGGER_STATE varchar(16) not null,
TRIGGER_TYPE varchar(8) not null,
START_TIME bigint(13) not null,
END_TIME bigint(13) null,
CALENDAR_NAME varchar(200) null,
MISFIRE_INSTR smallint(2) null,
JOB_DATA blob null,
primary key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
constraint QRTZ_TRIGGERS_ibfk_1
foreign key (SCHED_NAME, JOB_NAME, JOB_GROUP) references prosolo3.QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP)
)
;

create index IDX_QRTZ_T_C
	on QRTZ_TRIGGERS (SCHED_NAME, CALENDAR_NAME)
;

create index IDX_QRTZ_T_G
	on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP)
;

create index IDX_QRTZ_T_J
	on QRTZ_TRIGGERS (SCHED_NAME, JOB_NAME, JOB_GROUP)
;

create index IDX_QRTZ_T_JG
	on QRTZ_TRIGGERS (SCHED_NAME, JOB_GROUP)
;

create index IDX_QRTZ_T_NEXT_FIRE_TIME
	on QRTZ_TRIGGERS (SCHED_NAME, NEXT_FIRE_TIME)
;

create index IDX_QRTZ_T_NFT_MISFIRE
	on QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME)
;

create index IDX_QRTZ_T_NFT_ST
	on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE, NEXT_FIRE_TIME)
;

create index IDX_QRTZ_T_NFT_ST_MISFIRE
	on QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_STATE)
;

create index IDX_QRTZ_T_NFT_ST_MISFIRE_GRP
	on QRTZ_TRIGGERS (SCHED_NAME, MISFIRE_INSTR, NEXT_FIRE_TIME, TRIGGER_GROUP, TRIGGER_STATE)
;

create index IDX_QRTZ_T_N_G_STATE
	on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_GROUP, TRIGGER_STATE)
;

create index IDX_QRTZ_T_N_STATE
	on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, TRIGGER_STATE)
;

create index IDX_QRTZ_T_STATE
	on QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_STATE)
;

alter table QRTZ_BLOB_TRIGGERS
add constraint QRTZ_BLOB_TRIGGERS_ibfk_1
		foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references prosolo3.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
;

alter table QRTZ_CRON_TRIGGERS
add constraint QRTZ_CRON_TRIGGERS_ibfk_1
		foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references prosolo3.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
;

alter table QRTZ_SIMPLE_TRIGGERS
add constraint QRTZ_SIMPLE_TRIGGERS_ibfk_1
		foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references prosolo3.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
;

alter table QRTZ_SIMPROP_TRIGGERS
add constraint QRTZ_SIMPROP_TRIGGERS_ibfk_1
		foreign key (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) references prosolo3.QRTZ_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
;

create table activity1
(
	dtype varchar(31) not null,
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
difficulty int default '3' null,
duration bigint not null,
grading_mode varchar(255) not null,
max_points int not null,
result_type varchar(255) not null,
rubric_visibility varchar(255) not null,
student_can_edit_response char default 'F' null,
student_can_see_other_responses char default 'F' null,
type varchar(255) not null,
version bigint not null,
visible_for_unenrolled_students char default 'F' null,
accept_grades char default 'F' null,
consumer_key varchar(255) null,
launch_url varchar(255) null,
open_in_new_window char default 'F' null,
score_calculation varchar(255) null,
shared_secret varchar(255) null,
text text null,
link_name varchar(255) null,
url varchar(255) null,
url_type varchar(255) null,
created_by bigint null,
rubric bigint null
)
;

create index FK_51vt5mwt287fcjsoap7vsm5e2
	on activity1 (created_by)
;

create index FK_r28bfc9xvnj41036ky8xb7o98
	on activity1 (rubric)
;

create table activity1_captions
(
	activity1 bigint not null,
	captions bigint not null,
	primary key (activity1, captions),
constraint UK_b1n7gy4nb3oplj5v8jhali4rs
unique (captions),
constraint FK_8gchebrsymmudflj24yo48n79
foreign key (activity1) references prosolo3.activity1 (id)
)
;

create table activity1_files
(
	activity1 bigint not null,
	files bigint not null,
	primary key (activity1, files),
constraint UK_h9l61ttwr60t42cj53buu3rm0
unique (files),
constraint FK_m20elbr34b3oe5p86dcd773nl
foreign key (activity1) references prosolo3.activity1 (id)
)
;

create table activity1_links
(
	activity1 bigint not null,
	links bigint not null,
	primary key (activity1, links),
constraint UK_mpsbwvmwi7kc3a6sbcm8s9e5f
unique (links),
constraint FK_rhmeeesk0sdkg3qagjmf9lroj
foreign key (activity1) references prosolo3.activity1 (id)
)
;

create table activity1_tags
(
	activity1 bigint not null,
	tags bigint not null,
	primary key (activity1, tags),
constraint FK_hynwy4o71nraccfi6lu06ggsc
foreign key (activity1) references prosolo3.activity1 (id)
)
;

create index FK_4lcw676ek48pd37v9kr0mymim
	on activity1_tags (tags)
;

create table activity_assessment
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
points int not null,
type varchar(255) not null,
activity bigint not null,
competence_assessment bigint not null,
grade bigint null,
constraint UK_ir3b4ua2jau1q2mlw3xhwr4s7
unique (competence_assessment, activity),
constraint FK_o3uai8md6l5ilpc98ynjnlks0
foreign key (activity) references prosolo3.activity1 (id)
)
;

create index FK_o3uai8md6l5ilpc98ynjnlks0
	on activity_assessment (activity)
;

create index FK_pqe8ab74gg1e45r1muu3l2f48
	on activity_assessment (grade)
;

create table activity_criterion_assessment
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
comment varchar(255) null,
criterion bigint null,
level bigint null,
assessment bigint null,
constraint UK_b2gf4fq74omli2r1ftnq3ggob
unique (assessment, criterion),
constraint FK_py8sdhd47tm0swq51k6gtm8pt
foreign key (assessment) references prosolo3.activity_assessment (id)
)
;

create index FK_j41uvkbj5h50bv2okbqakkolf
	on activity_criterion_assessment (level)
;

create index FK_l9389a0coyr4791boeeb8reuj
	on activity_criterion_assessment (criterion)
;

create table activity_discussion_message
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
content varchar(9000) null,
updated datetime null,
discussion bigint null,
sender bigint null,
constraint FK_pc9oblcdq0wtaqumrpoghmym5
foreign key (discussion) references prosolo3.activity_assessment (id)
)
;

create index FK_gp4rjlpt100i22gadcrqsuyea
	on activity_discussion_message (sender)
;

create index FK_pc9oblcdq0wtaqumrpoghmym5
	on activity_discussion_message (discussion)
;

create table activity_discussion_participant
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
is_read char default 'T' null,
activity_discussion bigint null,
participant bigint null,
constraint FK_1aja12ermpd5n492qpm0wv51p
foreign key (activity_discussion) references prosolo3.activity_assessment (id)
)
;

create index FK_1aja12ermpd5n492qpm0wv51p
	on activity_discussion_participant (activity_discussion)
;

create index FK_klimyuls8rsoqt7v9ce5gqts9
	on activity_discussion_participant (participant)
;

alter table activity_discussion_message
add constraint FK_gp4rjlpt100i22gadcrqsuyea
		foreign key (sender) references prosolo3.activity_discussion_participant (id)
;

create table activity_grade
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
value int null
)
;

alter table activity_assessment
add constraint FK_pqe8ab74gg1e45r1muu3l2f48
		foreign key (grade) references prosolo3.activity_grade (id)
;

create table activity_wall_settings
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
chosen_filter varchar(255) not null,
course_id bigint not null
)
;

create table annotation1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
annotated_resource varchar(255) not null,
annotated_resource_id bigint not null,
annotation_type varchar(255) not null,
maker bigint null
)
;

create index FK_qg56tf64cpvwkc5p2q9bjoape
	on annotation1 (maker)
;

create table announcement
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
announcement_text varchar(5000) null,
created_by bigint not null,
credential bigint not null
)
;

create index FK_5n0gf0pgl54yxksqbrpg49jwu
	on announcement (credential)
;

create index FK_i5ssis4ksi4j8jsbb9xga1odi
	on announcement (created_by)
;

create table capability
(
	id bigint not null
		primary key,
	description varchar(255) null,
name varchar(255) null
)
;

create table capability_role
(
	capabilities bigint not null,
	roles bigint not null,
	primary key (capabilities, roles),
constraint FK_1j72vl0nwsv55q41b6ydg0hhd
foreign key (capabilities) references prosolo3.capability (id)
)
;

create index FK_bk8wqohsuock5hix7lnb84b0r
	on capability_role (roles)
;

create table comment1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
commented_resource_id bigint null,
instructor bit not null,
like_count int not null,
manager_comment char default 'F' null,
post_date datetime null,
resource_type varchar(255) not null,
parent_comment bigint null,
user bigint null,
constraint FK_q7hjkgwtsss4hrk6dfs022823
foreign key (parent_comment) references prosolo3.comment1 (id)
)
;

create index FK_cnb1hy7io12vb2fbi1jdmyalw
	on comment1 (user)
;

create index FK_q7hjkgwtsss4hrk6dfs022823
	on comment1 (parent_comment)
;

create index index_comment1_commented_resource_id_resource_type
	on comment1 (commented_resource_id, resource_type)
;

create table competence1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
archived char default 'F' null,
date_published datetime null,
duration bigint not null,
grading_mode varchar(255) not null,
learning_path_type varchar(255) not null,
max_points int not null,
published bit not null,
student_allowed_to_add_activities bit not null,
type varchar(255) not null,
version bigint not null,
visible_to_all char default 'F' null,
created_by bigint null,
first_learning_stage_competence bigint null,
learning_stage bigint null,
organization bigint not null,
original_version bigint null,
rubric bigint null,
constraint UK_3oyo6ynive1sdpf54j7lx3ggb
unique (first_learning_stage_competence, learning_stage),
constraint FK_rovwcltntpusa8fv9fk2ox4c5
foreign key (first_learning_stage_competence) references prosolo3.competence1 (id),
constraint FK_btejhdyv6se49077y8v2sw5sq
foreign key (original_version) references prosolo3.competence1 (id)
)
;

create index FK_11ldaw0qdiu7cbcxb74l4xxcy
	on competence1 (rubric)
;

create index FK_91ik0ggqkcwdde3bjco1da0gf
	on competence1 (organization)
;

create index FK_btejhdyv6se49077y8v2sw5sq
	on competence1 (original_version)
;

create index FK_j0jn9c30xitfotmamc9181go5
	on competence1 (created_by)
;

create index FK_lb3u5p37p6e33a9caoi64r1cj
	on competence1 (learning_stage)
;

create table competence1_tags
(
	competence1 bigint not null,
	tags bigint not null,
	primary key (competence1, tags),
constraint FK_njpyjqs5thuuyw5xwm1paihxf
foreign key (competence1) references prosolo3.competence1 (id)
)
;

create index FK_9lsylf57uy3m2se4ve4ameihu
	on competence1_tags (tags)
;

create table competence_activity1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
act_order int null,
activity bigint null,
competence bigint not null,
constraint FK_px25x3t4d4wxgujuocgi5mc51
foreign key (activity) references prosolo3.activity1 (id),
constraint FK_ka2ffpd609s5ug445vou0anhi
foreign key (competence) references prosolo3.competence1 (id)
)
;

create index FK_ka2ffpd609s5ug445vou0anhi
	on competence_activity1 (competence)
;

create index FK_px25x3t4d4wxgujuocgi5mc51
	on competence_activity1 (activity)
;

create table competence_assessment
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
approved bit null,
assessor_notified bit not null,
last_asked_for_assessment datetime null,
last_assessment datetime null,
message longtext null,
points int not null,
type varchar(255) not null,
assessor bigint null,
competence bigint not null,
student bigint not null,
constraint FK_k9y34nq3hj6fdbdaftoligfkc
foreign key (competence) references prosolo3.competence1 (id)
)
;

create index FK_k9y34nq3hj6fdbdaftoligfkc
	on competence_assessment (competence)
;

create index FK_lbo2bjanrdpkwedf9p6q6o248
	on competence_assessment (assessor)
;

create index FK_tiy256v79b1c8vfiest7m8piw
	on competence_assessment (student)
;

alter table activity_assessment
add constraint FK_8h8fnyxbc74ksc3qdhy48ic3t
		foreign key (competence_assessment) references prosolo3.competence_assessment (id)
;

create table competence_assessment_config
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
assessment_type varchar(255) not null,
enabled char default 'F' null,
competence bigint not null,
constraint UK_7u112t9m0qt8cdf1pwd2t6nam
unique (competence, assessment_type),
constraint FK_16qjgpixu90actkwbekfav4ui
foreign key (competence) references prosolo3.competence1 (id)
)
;

create table competence_assessment_discussion_participant
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
is_read char default 'T' null,
assessment bigint null,
participant bigint null,
constraint FK_136fh13xo3iys1yq0hti8xlnq
foreign key (assessment) references prosolo3.competence_assessment (id)
)
;

create index FK_136fh13xo3iys1yq0hti8xlnq
	on competence_assessment_discussion_participant (assessment)
;

create index FK_tk9bucqcwbukxgjkkckysawf5
	on competence_assessment_discussion_participant (participant)
;

create table competence_assessment_message
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
content varchar(9000) null,
updated datetime null,
assessment bigint null,
sender bigint null,
constraint FK_3cpxtlvcviq1sov7gb7fqgcfu
foreign key (assessment) references prosolo3.competence_assessment (id),
constraint FK_ioqggfbo6kiw9yr5ghv25rjgb
foreign key (sender) references prosolo3.competence_assessment_discussion_participant (id)
)
;

create index FK_3cpxtlvcviq1sov7gb7fqgcfu
	on competence_assessment_message (assessment)
;

create index FK_ioqggfbo6kiw9yr5ghv25rjgb
	on competence_assessment_message (sender)
;

create table competence_bookmark
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
competence bigint not null,
user bigint not null,
constraint UK_k4uyv9uljtw36cxchatxxhi7d
unique (competence, user),
constraint FK_pu790xeulgg9f166ehutb0p0v
foreign key (competence) references prosolo3.competence1 (id)
)
;

create index FK_7fwnajpqa9j4dfaciibethkll
	on competence_bookmark (user)
;

create table competence_criterion_assessment
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
comment varchar(255) null,
criterion bigint null,
level bigint null,
assessment bigint null,
constraint UK_cfbmdanr0gb6gaciqhgiee406
unique (assessment, criterion),
constraint FK_cyefgjjblo1bchk4vjw8fmwxi
foreign key (assessment) references prosolo3.competence_assessment (id)
)
;

create index FK_lkk5e6em0p1ccndm11xybktoy
	on competence_criterion_assessment (criterion)
;

create index FK_smjx7i6jja0xf458dwpfbuaos
	on competence_criterion_assessment (level)
;

create table competence_evidence
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
competence bigint not null,
evidence bigint not null
)
;

create index FK_8ih6dh7gn5uo6jwx9r3pt82sw
	on competence_evidence (competence)
;

create index FK_nctgqbkwam6fn84i27cyaav2p
	on competence_evidence (evidence)
;

create table competence_unit
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
competence bigint not null,
unit bigint not null,
constraint UK_7mbm0rkejlh524nk5ieyh24ml
unique (competence, unit),
constraint FK_d9i2062693wmimcdyvdr1ynhh
foreign key (competence) references prosolo3.competence1 (id)
)
;

create index FK_16il9gr3688yooyw91vsiis91
	on competence_unit (unit)
;

create table competence_user_group
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
inherited char default 'F' null,
privilege varchar(255) not null,
competence bigint not null,
inherited_from bigint null,
user_group bigint not null,
constraint FK_60mk2r1qk5llr3p901h5705y6
foreign key (competence) references prosolo3.competence1 (id)
)
;

create index FK_4b5s1r5gcdytwdbp2pr4rn4v5
	on competence_user_group (user_group)
;

create index FK_60mk2r1qk5llr3p901h5705y6
	on competence_user_group (competence)
;

create index FK_e9iw7p463kmjjsyuj9r5wmxpe
	on competence_user_group (inherited_from)
;

create table credential1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
archived char default 'F' null,
competence_order_mandatory bit not null,
default_number_of_students_per_instructor int not null,
delivery_end datetime null,
delivery_start datetime null,
duration bigint not null,
grading_mode varchar(255) not null,
manually_assign_students bit not null,
max_points int not null,
type varchar(255) not null,
version bigint not null,
visible_to_all char default 'F' null,
created_by bigint not null,
delivery_of bigint null,
first_learning_stage_credential bigint null,
learning_stage bigint null,
organization bigint not null,
rubric bigint null,
category bigint null,
constraint UK_6vclpmt9xfxxxybg8tdc4axwt
unique (first_learning_stage_credential, learning_stage),
constraint FK_ox2gmlf29yl2o60nwf28avccl
foreign key (delivery_of) references prosolo3.credential1 (id),
constraint FK_5ve9kkyxam4skfb85dy0n55ot
foreign key (first_learning_stage_credential) references prosolo3.credential1 (id)
)
;

create index FK_4s00xobf7567n67dwachjpsvm
	on credential1 (rubric)
;

create index FK_6nv6h30ry8lvl7g9lxj83oo2r
	on credential1 (category)
;

create index FK_gwgwt3bm30pwx6lxiukyx1o03
	on credential1 (created_by)
;

create index FK_n2vyrbyyls32y7s3aq1gyyh8o
	on credential1 (learning_stage)
;

create index FK_ox2gmlf29yl2o60nwf28avccl
	on credential1 (delivery_of)
;

create index FK_p1g20qojr5ahymmq2c2hkdqh3
	on credential1 (organization)
;

alter table announcement
add constraint FK_5n0gf0pgl54yxksqbrpg49jwu
		foreign key (credential) references prosolo3.credential1 (id)
;

alter table competence_user_group
add constraint FK_e9iw7p463kmjjsyuj9r5wmxpe
		foreign key (inherited_from) references prosolo3.credential1 (id)
;

create table credential1_blogs
(
	credential1 bigint not null,
	blogs bigint not null,
	constraint FK_p9mlitfs7rd27q1h6x6xqrtat
		foreign key (credential1) references prosolo3.credential1 (id)
)
;

create index FK_dgns5f815wa0piods87ak1unv
	on credential1_blogs (blogs)
;

create index FK_p9mlitfs7rd27q1h6x6xqrtat
	on credential1_blogs (credential1)
;

create table credential1_excluded_feed_sources
(
	credential1 bigint not null,
	excluded_feed_sources bigint not null,
	constraint FK_116l6dplwn565owmmjwq9nchy
		foreign key (credential1) references prosolo3.credential1 (id)
)
;

create index FK_116l6dplwn565owmmjwq9nchy
	on credential1_excluded_feed_sources (credential1)
;

create index FK_id94tkw373r7i4wpptlstjkt2
	on credential1_excluded_feed_sources (excluded_feed_sources)
;

create table credential1_hashtags
(
	credential1 bigint not null,
	hashtags bigint not null,
	primary key (credential1, hashtags),
constraint FK_5k9e4ud4n7uutdcfcp7x8xn2h
foreign key (credential1) references prosolo3.credential1 (id)
)
;

create index FK_2s3cange8x6y5lvxsgpulwmro
	on credential1_hashtags (hashtags)
;

create table credential1_tags
(
	credential1 bigint not null,
	tags bigint not null,
	primary key (credential1, tags),
constraint FK_o10dydyl21smpds2rwh7v8j4s
foreign key (credential1) references prosolo3.credential1 (id)
)
;

create index FK_e8b1q4xep66kcwhcxfag5a0cl
	on credential1_tags (tags)
;

create table credential_assessment
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
approved bit null,
assessor_notified bit not null,
last_asked_for_assessment datetime null,
last_assessment datetime null,
message longtext null,
points int not null,
review longtext null,
type varchar(255) not null,
assessor bigint null,
student bigint null,
target_credential bigint null,
assessed bit not null
)
;

create index FK_64x1nbncerttcr6ukwn1i10yy
	on credential_assessment (assessor)
;

create index FK_gvcyenwhr1wjurx8srk0l65m
	on credential_assessment (target_credential)
;

create index FK_o0soi8jus7un1g5yqyfrpo3rs
	on credential_assessment (student)
;

create table credential_assessment_config
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
assessment_type varchar(255) not null,
enabled char default 'F' null,
credential bigint not null,
constraint UK_e7upesftge7cjrbgqwqlbhypi
unique (credential, assessment_type),
constraint FK_amujhji7o6s62pk0sj65q290y
foreign key (credential) references prosolo3.credential1 (id)
)
;

create table credential_assessment_discussion_participant
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
is_read char default 'T' null,
assessment bigint null,
participant bigint null,
constraint FK_lq0fgfb3gmn18bita5oybkxft
foreign key (assessment) references prosolo3.credential_assessment (id)
)
;

create index FK_lq0fgfb3gmn18bita5oybkxft
	on credential_assessment_discussion_participant (assessment)
;

create index FK_safwo8xgq1h61ttmdd9kob4hp
	on credential_assessment_discussion_participant (participant)
;

create table credential_assessment_message
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
content varchar(9000) null,
updated datetime null,
assessment bigint null,
sender bigint null,
constraint FK_dlo6gfx7ifgbh5dg06tvyai67
foreign key (assessment) references prosolo3.credential_assessment (id),
constraint FK_e1kd4wxe27u2mh91ql011uj1q
foreign key (sender) references prosolo3.credential_assessment_discussion_participant (id)
)
;

create index FK_dlo6gfx7ifgbh5dg06tvyai67
	on credential_assessment_message (assessment)
;

create index FK_e1kd4wxe27u2mh91ql011uj1q
	on credential_assessment_message (sender)
;

create table credential_bookmark
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
credential bigint not null,
user bigint not null,
constraint UK_huxm5oy8mxf4gcckxuy6m5n9a
unique (credential, user),
constraint FK_d125vpplcb2nwcw100ck2ch7e
foreign key (credential) references prosolo3.credential1 (id)
)
;

create index FK_n5fkjwxicertabyarbvju43c3
	on credential_bookmark (user)
;

create table credential_category
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
organization bigint not null,
constraint UK_6mnq89yqpnfilldvrsyblvavb
unique (organization, title)
)
;

alter table credential1
add constraint FK_6nv6h30ry8lvl7g9lxj83oo2r
		foreign key (category) references prosolo3.credential_category (id)
;

create table credential_competence1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
comp_order int null,
competence bigint not null,
credential bigint not null,
constraint FK_4j36i3b2r10ewvv85c9st8c34
foreign key (competence) references prosolo3.competence1 (id),
constraint FK_nw0ci3p9g1hiy4yan5w664to9
foreign key (credential) references prosolo3.credential1 (id)
)
;

create index FK_4j36i3b2r10ewvv85c9st8c34
	on credential_competence1 (competence)
;

create index FK_nw0ci3p9g1hiy4yan5w664to9
	on credential_competence1 (credential)
;

create table credential_competence_assessment
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
competence_assessment bigint not null,
credential_assessment bigint not null,
constraint UK_cq14nxxgq3qb98h7v9dd9ubsj
unique (credential_assessment, competence_assessment),
constraint FK_hs8gjt68hymusnmsuuar20med
foreign key (competence_assessment) references prosolo3.competence_assessment (id),
constraint FK_lvmp3fd0dxsxestak8yervbrq
foreign key (credential_assessment) references prosolo3.credential_assessment (id)
)
;

create index FK_hs8gjt68hymusnmsuuar20med
	on credential_competence_assessment (competence_assessment)
;

create table credential_criterion_assessment
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
comment varchar(255) null,
criterion bigint null,
level bigint null,
assessment bigint null,
constraint UK_bma5i20uxkurqqatu2k4aetlf
unique (assessment, criterion),
constraint FK_nn5t74iqwu7v2isk9xj3dtg7u
foreign key (assessment) references prosolo3.credential_assessment (id)
)
;

create index FK_cgpk6q28vp6usjal5rlce9s9i
	on credential_criterion_assessment (criterion)
;

create index FK_p24wh1b1sc1m8m15ler4xbuvv
	on credential_criterion_assessment (level)
;

create table credential_instructor
(
	id bigint not null
		primary key,
	date_assigned datetime null,
	max_number_of_students int not null,
	credential bigint not null,
	user bigint not null,
	constraint UK_8o1ljk3ajomrhnh8d5qfmcnls
		unique (user, credential),
constraint FK_4c65jghtljkorcy98mnko26lt
foreign key (credential) references prosolo3.credential1 (id)
)
;

create index FK_4c65jghtljkorcy98mnko26lt
	on credential_instructor (credential)
;

create table credential_unit
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
credential bigint not null,
unit bigint not null,
constraint UK_piaytj54rr4klingo78qx9ylt
unique (credential, unit),
constraint FK_10qyaft78ol2toenfodqdwc8g
foreign key (credential) references prosolo3.credential1 (id)
)
;

create index FK_bcknt360dtw8rh53cphe97q45
	on credential_unit (unit)
;

create table credential_user_group
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
privilege varchar(255) not null,
credential bigint not null,
user_group bigint not null,
constraint UK_j07j1pph3s8y22el92gth2yy5
unique (credential, user_group, privilege),
constraint FK_30jl08j0war3rn3u23m10uyq8
foreign key (credential) references prosolo3.credential1 (id)
)
;

create index FK_m5cvxlf67t55is1xw2gxfar0i
	on credential_user_group (user_group)
;

create table criterion
(
	dtype varchar(31) not null,
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
criterion_order int not null,
points double default '0' not null,
rubric bigint null,
constraint UK_e1f4a4t2ns27fm40ulwry89u8
unique (title, rubric)
)
;

create index FK_o7ovdmcl7pmxtdnuk3iyn2sep
	on criterion (rubric)
;

alter table activity_criterion_assessment
add constraint FK_l9389a0coyr4791boeeb8reuj
		foreign key (criterion) references prosolo3.criterion (id)
;

alter table competence_criterion_assessment
add constraint FK_lkk5e6em0p1ccndm11xybktoy
		foreign key (criterion) references prosolo3.criterion (id)
;

alter table credential_criterion_assessment
add constraint FK_cgpk6q28vp6usjal5rlce9s9i
		foreign key (criterion) references prosolo3.criterion (id)
;

create table criterion_level
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
criterion bigint not null,
level bigint not null,
constraint UK_pquo8f8sy5d20h8f5hmivseq8
unique (criterion, level),
constraint FK_9l1253t7v8atu5lytujimj09n
foreign key (criterion) references prosolo3.criterion (id)
)
;

create index FK_t0ynrcrm7kt8i1luj4onvhjv2
	on criterion_level (level)
;

create table feed_entry
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
image varchar(255) null,
link varchar(255) null,
relevance double not null,
feed_source bigint null,
maker bigint null,
subscribed_user bigint null
)
;

create index FK_5mympaondc7gotjymqgd05l4i
	on feed_entry (subscribed_user)
;

create index FK_cgxvubmhf09musstvw2u00y67
	on feed_entry (maker)
;

create index FK_ffdgn3747cqpgdiwhxlarhp8m
	on feed_entry (feed_source)
;

create table feed_entry_hashtags
(
	feed_entry bigint not null,
	hashtags varchar(255) null,
constraint FK_es7wwjpedyq7gxlsk0lexcj5x
foreign key (feed_entry) references prosolo3.feed_entry (id)
)
;

create index FK_es7wwjpedyq7gxlsk0lexcj5x
	on feed_entry_hashtags (feed_entry)
;

create table feed_source
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
last_check datetime null,
link varchar(500) null
)
;

alter table credential1_blogs
add constraint FK_dgns5f815wa0piods87ak1unv
		foreign key (blogs) references prosolo3.feed_source (id)
;

alter table credential1_excluded_feed_sources
add constraint FK_id94tkw373r7i4wpptlstjkt2
		foreign key (excluded_feed_sources) references prosolo3.feed_source (id)
;

alter table feed_entry
add constraint FK_ffdgn3747cqpgdiwhxlarhp8m
		foreign key (feed_source) references prosolo3.feed_source (id)
;

create table feeds_digest
(
	dtype varchar(50) not null,
	id bigint not null
		primary key,
	created datetime null,
	date_from datetime null,
	number_of_users_that_got_email bigint not null,
	time_frame varchar(255) not null,
date_to datetime null,
credential bigint null,
feeds_subscriber bigint null,
constraint FK_hcx2rl68idmgg2sfpnbqmv1tv
foreign key (credential) references prosolo3.credential1 (id)
)
;

create index FK_hcx2rl68idmgg2sfpnbqmv1tv
	on feeds_digest (credential)
;

create index FK_j4nanu4eoo6lobqy4v64tegmf
	on feeds_digest (feeds_subscriber)
;

create table feeds_digest_entries
(
	feeds_digest bigint not null,
	entries bigint not null,
	constraint FK_bhda4lck3rkx4tduxm7fhq8wo
		foreign key (feeds_digest) references prosolo3.feeds_digest (id),
constraint FK_sj34xafykjukgs471aa2w9anj
foreign key (entries) references prosolo3.feed_entry (id)
)
;

create index FK_bhda4lck3rkx4tduxm7fhq8wo
	on feeds_digest_entries (feeds_digest)
;

create index FK_sj34xafykjukgs471aa2w9anj
	on feeds_digest_entries (entries)
;

create table followed_entity
(
	dtype varchar(31) not null,
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
required_to_follow char default 'F' null,
started_following datetime null,
user bigint null,
followed_user bigint null,
constraint UK_6w0pe4f2eii8eov3epmwk849e
unique (user, followed_user)
)
;

create index FK_4eglof45iulhoj8j2qudvbuxm
	on followed_entity (followed_user)
;

create table hibernate_sequences
(
  sequence_name varchar(255) null,
  sequence_next_hi_value int null
)
;

create table innodb_lock_monitor
(
  a int null
)
;

create table learning_evidence
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
type varchar(255) not null,
url varchar(255) null,
organization bigint not null,
user bigint not null
)
;

create index FK_qb3lm6liqglp03b789fl0sdvb
	on learning_evidence (organization)
;

create index FK_te80g9rqpmjnlpmff23h0we5x
	on learning_evidence (user)
;

alter table competence_evidence
add constraint FK_nctgqbkwam6fn84i27cyaav2p
		foreign key (evidence) references prosolo3.learning_evidence (id)
;

create table learning_evidence_tags
(
	learning_evidence bigint not null,
	tags bigint not null,
	primary key (learning_evidence, tags),
constraint FK_ge8rmsf18x8fwwntcnof1w2c3
foreign key (learning_evidence) references prosolo3.learning_evidence (id)
)
;

create index FK_2i6sole2k1kwkjbgsm2u6aoff
	on learning_evidence_tags (tags)
;

create table learning_stage
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
learning_stage_order int null,
organization bigint not null,
constraint UK_atmtmfxbwira99uums40pt8q
unique (organization, title)
)
;

alter table competence1
add constraint FK_lb3u5p37p6e33a9caoi64r1cj
		foreign key (learning_stage) references prosolo3.learning_stage (id)
;

alter table credential1
add constraint FK_n2vyrbyyls32y7s3aq1gyyh8o
		foreign key (learning_stage) references prosolo3.learning_stage (id)
;

create table level
(
	dtype varchar(31) not null,
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
level_order int not null,
points double default '0' not null,
points_from double default '0' not null,
points_to double default '0' not null,
rubric bigint null,
constraint UK_eo5k8jw0yq5kypsaddagxuoo8
unique (title, rubric)
)
;

create index FK_rtffghkdqv9g81wd3ecvjemdv
	on level (rubric)
;

alter table activity_criterion_assessment
add constraint FK_j41uvkbj5h50bv2okbqakkolf
		foreign key (level) references prosolo3.level (id)
;

alter table competence_criterion_assessment
add constraint FK_smjx7i6jja0xf458dwpfbuaos
		foreign key (level) references prosolo3.level (id)
;

alter table credential_criterion_assessment
add constraint FK_p24wh1b1sc1m8m15ler4xbuvv
		foreign key (level) references prosolo3.level (id)
;

alter table criterion_level
add constraint FK_t0ynrcrm7kt8i1luj4onvhjv2
		foreign key (level) references prosolo3.level (id)
;

create table locale_settings
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
language varchar(255) null,
region varchar(255) null
)
;

create table lti_consumer
(
	id bigint not null
		primary key,
	deleted bit not null,
	capabilities varchar(2000) null,
key_lti_one varchar(255) not null,
key_lti_two varchar(255) null,
secret_lti_one varchar(255) not null,
secret_lti_two varchar(255) null
)
;

create table lti_service
(
	id bigint not null
		primary key,
	deleted bit not null,
	actions varchar(255) null,
endpoint varchar(255) null,
formats varchar(1000) null,
consumer bigint null,
constraint FK_j04buydudvuvpfslrit8lht1s
foreign key (consumer) references prosolo3.lti_consumer (id)
)
;

create index FK_j04buydudvuvpfslrit8lht1s
	on lti_service (consumer)
;

create table lti_tool
(
	id bigint not null
		primary key,
	deleted bit not null,
	activity_id bigint not null,
	code varchar(255) null,
competence_id bigint not null,
credential_id bigint not null,
custom_css varchar(10000) null,
description varchar(2000) null,
enabled bit not null,
launch_url varchar(255) null,
name varchar(255) null,
resource_name varchar(255) null,
tool_key varchar(255) null,
tool_type varchar(255) not null,
created_by bigint not null,
organization bigint not null,
tool_set bigint not null,
unit bigint null,
user_group bigint null
)
;

create index FK_64dtvy9vcghm1b7cv0ymsgg4p
	on lti_tool (user_group)
;

create index FK_6eimoaon171t8shrxhqs0dils
	on lti_tool (organization)
;

create index FK_75s355fn31ogkxv4fwj6ve1hm
	on lti_tool (unit)
;

create index FK_f8glreyaxbnn1wq3ox0b76igm
	on lti_tool (tool_set)
;

create index FK_n8iw9ueffp6ceo31pnuensikq
	on lti_tool (created_by)
;

create table lti_tool_set
(
	id bigint not null
		primary key,
	deleted bit not null,
	product_code varchar(255) null,
registration_url varchar(255) null,
consumer bigint not null,
constraint FK_rxmmq9va47gyeome10lkj5ktw
foreign key (consumer) references prosolo3.lti_consumer (id)
)
;

create index FK_rxmmq9va47gyeome10lkj5ktw
	on lti_tool_set (consumer)
;

alter table lti_tool
add constraint FK_f8glreyaxbnn1wq3ox0b76igm
		foreign key (tool_set) references prosolo3.lti_tool_set (id)
;

create table lti_user
(
	id bigint not null
		primary key,
	deleted bit not null,
	email varchar(255) null,
name varchar(255) null,
user_id varchar(255) null,
consumer bigint not null,
user bigint null,
constraint FK_406o6qhyoo4xcc0ybhrs47xge
foreign key (consumer) references prosolo3.lti_consumer (id)
)
;

create index FK_406o6qhyoo4xcc0ybhrs47xge
	on lti_user (consumer)
;

create index FK_hxb07pfijibeqr1mm8qnnam0i
	on lti_user (user)
;

create table message
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
content varchar(9000) null,
created_timestamp datetime null,
message_thread bigint null,
sender bigint null
)
;

create index FK_a3km2kv42i1xu571ta911f9dc
	on message (sender)
;

create index FK_jgrx8hvls0cxtwdet9tu5tl3o
	on message (message_thread)
;

create table message_participant
(
id bigint not null
primary key,
is_read char default 'F' null,
sender char default 'F' null,
participant bigint null
)
;

create index FK_nt68vh666kew9wsx344d2tej8
	on message_participant (participant)
;

create table message_thread
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
started datetime null,
updated datetime null,
subject varchar(255) null,
creator bigint null
)
;

create index FK_owblue8e7depfe80u0tut6vnp
	on message_thread (creator)
;

alter table message
add constraint FK_jgrx8hvls0cxtwdet9tu5tl3o
		foreign key (message_thread) references prosolo3.message_thread (id)
;

create table notification1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
link varchar(255) null,
notify_by_email char default 'T' null,
object_id bigint not null,
object_owner char default 'F' null,
object_type varchar(255) null,
is_read char default 'F' null,
section varchar(255) not null,
target_id bigint not null,
target_type varchar(255) null,
type varchar(255) not null,
actor bigint null,
receiver bigint null
)
;

create index FK_8ffsbfqrwx0l4fcwtir4eogs8
	on notification1 (actor)
;

create index FK_eu8xhjr832wpkchfxn2a4ne8f
	on notification1 (receiver)
;

create table notification_settings
(
	id bigint not null
		primary key,
	subscribed_email char default 'F' null,
	type varchar(255) not null,
user bigint null,
constraint UK_fhuhqotesvv46234bv4r36w19
unique (type, user)
)
;

create index FK_ajh6l15bdar6g8o7d01b52j3t
	on notification_settings (user)
;

create table oauth_access_token
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
profile_link varchar(255) null,
profile_name varchar(255) null,
service varchar(255) not null,
token varchar(255) null,
token_secret varchar(255) null,
user_id bigint null,
user bigint null
)
;

create index FK_qwnyxe99obn5c7jamrh80tojn
	on oauth_access_token (user)
;

create table observation
(
id bigint not null
primary key,
creation_date datetime null,
edited bit not null,
message longtext null,
note longtext null,
created_by bigint null,
created_for bigint null
)
;

create index FK_f238497jjp2hx63imjj1ndnbv
	on observation (created_by)
;

create index FK_owstmit5g7fnsggixn5tmfmd7
	on observation (created_for)
;

create table observation_suggestion
(
	observation bigint not null,
	suggestions bigint not null,
	primary key (observation, suggestions),
constraint FK_q5ec7t3ca2ihn36eifg4u7qw4
foreign key (observation) references prosolo3.observation (id)
)
;

create index FK_rxdpfym9lrnrum8yak52i6ya5
	on observation_suggestion (suggestions)
;

create table observation_symptom
(
	observation bigint not null,
	symptoms bigint not null,
	primary key (observation, symptoms),
constraint FK_2r1fs2ynpa1istpwqlmpc52yl
foreign key (observation) references prosolo3.observation (id)
)
;

create index FK_axke49cj36g768x69cpf9t57l
	on observation_symptom (symptoms)
;

create table openidaccount
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
openidprovider varchar(255) not null,
validated_id varchar(255) null,
user bigint null
)
;

create index FK_mrch25e7jcip15grmcp54039i
	on openidaccount (user)
;

create table organization
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
learning_in_stages_enabled char default 'F' null,
constraint UK_98elant9gwyioac8t0br67o5p
unique (title)
)
;

alter table competence1
add constraint FK_91ik0ggqkcwdde3bjco1da0gf
		foreign key (organization) references prosolo3.organization (id)
;

alter table credential1
add constraint FK_p1g20qojr5ahymmq2c2hkdqh3
		foreign key (organization) references prosolo3.organization (id)
;

alter table credential_category
add constraint FK_r8ao03qj30gfud7h29sk1a4hr
		foreign key (organization) references prosolo3.organization (id)
;

alter table learning_evidence
add constraint FK_qb3lm6liqglp03b789fl0sdvb
		foreign key (organization) references prosolo3.organization (id)
;

alter table learning_stage
add constraint FK_kmqfvn3rumkbm0b8dsgfbcnm6
		foreign key (organization) references prosolo3.organization (id)
;

alter table lti_tool
add constraint FK_6eimoaon171t8shrxhqs0dils
		foreign key (organization) references prosolo3.organization (id)
;

create table outcome
(
	dtype varchar(31) not null,
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
result int null,
activity bigint not null
)
;

create index FK_c6sy38ram71uel2jdeotexor3
	on outcome (activity)
;

create table registration_key
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
registration_type varchar(255) not null,
uid varchar(255) null,
constraint UK_ran40qjbh70o6m8q25xu4qxc9
unique (uid)
)
;

create table reset_key
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
invalid char not null,
uid varchar(255) null,
user bigint null,
constraint UK_4os64y5nsc6cbbd73qj4lujb1
unique (uid)
)
;

create index FK_fxv54xs4m5y1tddmi2errcesu
	on reset_key (user)
;

create table resource_link
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
id_parameter_name varchar(255) null,
link_name varchar(255) null,
url varchar(255) null
)
;

alter table activity1_captions
add constraint FK_b1n7gy4nb3oplj5v8jhali4rs
		foreign key (captions) references prosolo3.resource_link (id)
;

alter table activity1_files
add constraint FK_h9l61ttwr60t42cj53buu3rm0
		foreign key (files) references prosolo3.resource_link (id)
;

alter table activity1_links
add constraint FK_mpsbwvmwi7kc3a6sbcm8s9e5f
		foreign key (links) references prosolo3.resource_link (id)
;

create table resource_settings
(
id bigint not null
primary key,
goal_acceptance_dependend_on_competence char default 'F' null,
individual_competences_can_not_be_evaluated char default 'F' null,
selected_users_can_do_evaluation char default 'F' null,
user_can_create_competence char not null
)
;

create table role
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
system char not null
)
;

alter table capability_role
add constraint FK_bk8wqohsuock5hix7lnb84b0r
		foreign key (roles) references prosolo3.role (id)
;

create table rubric
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
ready_to_use char default 'F' not null,
rubric_type varchar(255) not null,
creator bigint null,
organization bigint not null,
constraint UK_4fkcsmege20c5mr184i7sw8a8
unique (title, organization),
constraint FK_lf8lryt4i4c66o0lot08j3cu7
foreign key (organization) references prosolo3.organization (id)
)
;

create index FK_6peou3uwnq3v5xoirhfg4buwh
	on rubric (creator)
;

create index FK_lf8lryt4i4c66o0lot08j3cu7
	on rubric (organization)
;

alter table activity1
add constraint FK_r28bfc9xvnj41036ky8xb7o98
		foreign key (rubric) references prosolo3.rubric (id)
;

alter table competence1
add constraint FK_11ldaw0qdiu7cbcxb74l4xxcy
		foreign key (rubric) references prosolo3.rubric (id)
;

alter table credential1
add constraint FK_4s00xobf7567n67dwachjpsvm
		foreign key (rubric) references prosolo3.rubric (id)
;

alter table criterion
add constraint FK_o7ovdmcl7pmxtdnuk3iyn2sep
		foreign key (rubric) references prosolo3.rubric (id)
;

alter table level
add constraint FK_rtffghkdqv9g81wd3ecvjemdv
		foreign key (rubric) references prosolo3.rubric (id)
;

create table rubric_unit
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
rubric bigint not null,
unit bigint not null,
constraint UK_9ir0vsnl7lrvdnm4nw907xc0h
unique (rubric, unit),
constraint FK_85vusepoi725ebye515abgc2
foreign key (rubric) references prosolo3.rubric (id)
)
;

create index FK_2rbgvha4m64j067s9p7f52361
	on rubric_unit (unit)
;

create table seen_announcement
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
announcement bigint not null,
user bigint not null,
constraint FK_63g4k687qg4rdy0tw55bek8ph
foreign key (announcement) references prosolo3.announcement (id)
)
;

create index FK_63g4k687qg4rdy0tw55bek8ph
	on seen_announcement (announcement)
;

create index FK_8xib2pv1vo1dkrj7mwkc7stt0
	on seen_announcement (user)
;

create table social_activity1
(
	dtype varchar(100) not null,
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
comments_disabled char default 'F' null,
last_action datetime null,
like_count int not null,
text longtext null,
rich_content_content_type varchar(255) null,
rich_content_description varchar(9000) null,
rich_content_embed_id varchar(255) null,
rich_content_image_size varchar(255) null,
rich_content_image_url varchar(255) null,
rich_content_last_indexing_update datetime null,
rich_content_link varchar(255) null,
rich_content_title varchar(255) null,
twitter_poster_avatar_url varchar(255) null,
twitter_comment varchar(255) null,
twitter_poster_name varchar(255) null,
twitter_poster_nickname varchar(255) null,
twitter_post_url varchar(255) null,
twitter_poster_profile_url varchar(255) null,
retweet char default 'F' null,
twitter_user_type int null,
actor bigint null,
comment_object bigint null,
activity_target bigint null,
target_activity_object bigint null,
competence_target bigint null,
target_competence_object bigint null,
credential_object bigint null,
post_object bigint null,
unit bigint null,
constraint FK_n6q7e5h3tjgxn0p2m0wtdf05y
foreign key (comment_object) references prosolo3.comment1 (id),
constraint FK_fg2k09y8coydd8lt8mxmsyv8d
foreign key (activity_target) references prosolo3.activity1 (id),
constraint FK_dvy0dowvaju4eq844k2qbfkm4
foreign key (competence_target) references prosolo3.competence1 (id),
constraint FK_r6x1eu9v3w7v4yrq67inwon9i
foreign key (credential_object) references prosolo3.credential1 (id),
constraint FK_3se0xx7kf9aw5vgmraucntlbv
foreign key (post_object) references prosolo3.social_activity1 (id)
)
;

create index FK_3se0xx7kf9aw5vgmraucntlbv
	on social_activity1 (post_object)
;

create index FK_9aicxka2n6jvmd38yw2rokm0c
	on social_activity1 (unit)
;

create index FK_cl6hufyquct3r8e3kj5foof4p
	on social_activity1 (target_activity_object)
;

create index FK_dvy0dowvaju4eq844k2qbfkm4
	on social_activity1 (competence_target)
;

create index FK_fg2k09y8coydd8lt8mxmsyv8d
	on social_activity1 (activity_target)
;

create index FK_ic66jfo7hmtbua887739e9hv6
	on social_activity1 (target_competence_object)
;

create index FK_n6q7e5h3tjgxn0p2m0wtdf05y
	on social_activity1 (comment_object)
;

create index FK_r6x1eu9v3w7v4yrq67inwon9i
	on social_activity1 (credential_object)
;

create index index_social_activity1_actor_last_action_id
	on social_activity1 (actor, last_action, id)
;

create index index_social_activity1_last_action_id
	on social_activity1 (last_action, id)
;

create table social_activity1_comments
(
	social_activity1 bigint not null,
	comments bigint not null,
	constraint UK_5lk34om0v81djsqof46lemooh
		unique (comments),
constraint FK_icouxfgkx9j575thujmbiifyw
foreign key (social_activity1) references prosolo3.social_activity1 (id),
constraint FK_5lk34om0v81djsqof46lemooh
foreign key (comments) references prosolo3.comment1 (id)
)
;

create index FK_icouxfgkx9j575thujmbiifyw
	on social_activity1_comments (social_activity1)
;

create table social_activity1_hashtags
(
	social_activity1 bigint not null,
	hashtags bigint not null,
	primary key (social_activity1, hashtags),
constraint FK_7puhxui6hgij23pw2s0jpwu8s
foreign key (social_activity1) references prosolo3.social_activity1 (id)
)
;

create index FK_dmmihaliyqi3d04t7vr50bept
	on social_activity1_hashtags (hashtags)
;

create table social_activity_config
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
hidden char default 'F' null,
social_activity bigint null,
user bigint null,
constraint FK_iji9gdpcntyigmeky9rc8w9e2
foreign key (social_activity) references prosolo3.social_activity1 (id)
)
;

create index FK_iji9gdpcntyigmeky9rc8w9e2
	on social_activity_config (social_activity)
;

create index FK_p4ouuukgvnn6j8to9pc5xnlq4
	on social_activity_config (user)
;

create table social_network_account
(
	id bigint not null
		primary key,
	link varchar(255) null,
social_network varchar(255) not null
)
;

create table suggestion
(
	id bigint not null
		primary key,
	description varchar(255) null
)
;

alter table observation_suggestion
add constraint FK_rxdpfym9lrnrum8yak52i6ya5
		foreign key (suggestions) references prosolo3.suggestion (id)
;

create table symptom
(
	id bigint not null
		primary key,
	description varchar(255) null
)
;

alter table observation_symptom
add constraint FK_axke49cj36g768x69cpf9t57l
		foreign key (symptoms) references prosolo3.symptom (id)
;

create table tag
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null
)
;

alter table activity1_tags
add constraint FK_4lcw676ek48pd37v9kr0mymim
		foreign key (tags) references prosolo3.tag (id)
;

alter table competence1_tags
add constraint FK_9lsylf57uy3m2se4ve4ameihu
		foreign key (tags) references prosolo3.tag (id)
;

alter table credential1_hashtags
add constraint FK_2s3cange8x6y5lvxsgpulwmro
		foreign key (hashtags) references prosolo3.tag (id)
;

alter table credential1_tags
add constraint FK_e8b1q4xep66kcwhcxfag5a0cl
		foreign key (tags) references prosolo3.tag (id)
;

alter table learning_evidence_tags
add constraint FK_2i6sole2k1kwkjbgsm2u6aoff
		foreign key (tags) references prosolo3.tag (id)
;

alter table social_activity1_hashtags
add constraint FK_dmmihaliyqi3d04t7vr50bept
		foreign key (hashtags) references prosolo3.tag (id)
;

create table target_activity1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
added bit not null,
common_score int not null,
completed bit not null,
date_completed datetime null,
number_of_attempts int not null,
act_order int null,
result longtext null,
result_post_date datetime null,
time_spent bigint not null,
activity bigint not null,
target_competence bigint not null,
constraint FK_5y8xtibcjsl3q1g2pyjp5v478
foreign key (activity) references prosolo3.activity1 (id)
)
;

create index FK_5y8xtibcjsl3q1g2pyjp5v478
	on target_activity1 (activity)
;

create index FK_fn87y61fx7126byyp4gx2i38v
	on target_activity1 (target_competence)
;

alter table outcome
add constraint FK_c6sy38ram71uel2jdeotexor3
		foreign key (activity) references prosolo3.target_activity1 (id)
;

alter table social_activity1
add constraint FK_cl6hufyquct3r8e3kj5foof4p
		foreign key (target_activity_object) references prosolo3.target_activity1 (id)
;

create table target_competence1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
date_completed datetime null,
hidden_from_profile bit not null,
next_activity_to_learn_id bigint not null,
progress int not null,
competence bigint not null,
user bigint not null,
constraint UK_hyygpgbqj161vj4fkdgxupoog
unique (competence, user),
constraint FK_2jik1pn632ups2li1pmvuahdd
foreign key (competence) references prosolo3.competence1 (id)
)
;

create index FK_62p0o7vw29vwnqc58g1hu2vai
	on target_competence1 (user)
;

alter table competence_evidence
add constraint FK_8ih6dh7gn5uo6jwx9r3pt82sw
		foreign key (competence) references prosolo3.target_competence1 (id)
;

alter table social_activity1
add constraint FK_ic66jfo7hmtbua887739e9hv6
		foreign key (target_competence_object) references prosolo3.target_competence1 (id)
;

alter table target_activity1
add constraint FK_fn87y61fx7126byyp4gx2i38v
		foreign key (target_competence) references prosolo3.target_competence1 (id)
;

create table target_credential1
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
assigned_to_instructor bit not null,
cluster varchar(255) null,
cluster_name varchar(255) null,
date_finished datetime null,
date_started datetime null,
final_review varchar(255) null,
hidden_from_profile bit not null,
last_action datetime null,
next_competence_to_learn_id bigint not null,
progress int not null,
credential bigint not null,
instructor bigint null,
user bigint not null,
competence_assessments_displayed bit default b'1' null,
credential_assessments_displayed bit default b'1' null,
evidence_displayed bit default b'1' null,
constraint UK_hxf01pvgri60h660un28e1w2q
unique (credential, user),
constraint FK_s3fmqrd1ct10kj04au40cuqhr
foreign key (credential) references prosolo3.credential1 (id),
constraint FK_7usnugjbauedaxofmlt5mlhau
foreign key (instructor) references prosolo3.credential_instructor (id)
)
;

create index FK_76pg8xnfhsda0bvvq7wvwl5xp
	on target_credential1 (user)
;

create index FK_7usnugjbauedaxofmlt5mlhau
	on target_credential1 (instructor)
;

alter table credential_assessment
add constraint FK_gvcyenwhr1wjurx8srk0l65m
		foreign key (target_credential) references prosolo3.target_credential1 (id)
;

create table terms_of_use
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
accepted char not null,
date datetime null
)
;

create table testTable
(
  a char null
)
;

create table thread_participant
(
	id bigint not null
		primary key,
	archived char default 'F' null,
	deleted char default 'F' null,
	is_read char default 'F' null,
	show_messages_from datetime null,
	last_read_message bigint null,
	message_thread bigint null,
	user bigint null,
	constraint FK_2o8pq3r6bxabp5b73ok8qgrcx
		foreign key (last_read_message) references prosolo3.message (id),
constraint FK_ilywn1b6wdhl4ymswbb75ayal
foreign key (message_thread) references prosolo3.message_thread (id)
)
;

create index FK_2o8pq3r6bxabp5b73ok8qgrcx
	on thread_participant (last_read_message)
;

create index FK_ilywn1b6wdhl4ymswbb75ayal
	on thread_participant (message_thread)
;

create index FK_ks01qd3o7158kirgyfpkw6cbb
	on thread_participant (user)
;

alter table message
add constraint FK_a3km2kv42i1xu571ta911f9dc
		foreign key (sender) references prosolo3.thread_participant (id)
;

create table unit
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
organization bigint not null,
parent_unit bigint null,
welcome_message text null,
constraint UK_hotnqwr5osir4ryygavjto7ac
unique (title, organization),
constraint FK_oxsulo29jty26qck7xmhhf9f1
foreign key (organization) references prosolo3.organization (id),
constraint FK_8e9s0ln9wmq4ydgbnhhyfjgs5
foreign key (parent_unit) references prosolo3.unit (id)
)
;

create index FK_8e9s0ln9wmq4ydgbnhhyfjgs5
	on unit (parent_unit)
;

create index FK_oxsulo29jty26qck7xmhhf9f1
	on unit (organization)
;

alter table competence_unit
add constraint FK_16il9gr3688yooyw91vsiis91
		foreign key (unit) references prosolo3.unit (id)
;

alter table credential_unit
add constraint FK_bcknt360dtw8rh53cphe97q45
		foreign key (unit) references prosolo3.unit (id)
;

alter table lti_tool
add constraint FK_75s355fn31ogkxv4fwj6ve1hm
		foreign key (unit) references prosolo3.unit (id)
;

alter table rubric_unit
add constraint FK_2rbgvha4m64j067s9p7f52361
		foreign key (unit) references prosolo3.unit (id)
;

alter table social_activity1
add constraint FK_9aicxka2n6jvmd38yw2rokm0c
		foreign key (unit) references prosolo3.unit (id)
;

create table unit_role_membership
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
role bigint not null,
unit bigint not null,
user bigint not null,
constraint UK_cvpfera4fi0vhmc4jd8c6a1j8
unique (user, unit, role),
constraint FK_d5bj6kukr793taljwratj3l05
foreign key (role) references prosolo3.role (id),
constraint FK_gn7knglhp5coc24iqd4yeevb0
foreign key (unit) references prosolo3.unit (id)
)
;

create index FK_d5bj6kukr793taljwratj3l05
	on unit_role_membership (role)
;

create index FK_gn7knglhp5coc24iqd4yeevb0
	on unit_role_membership (unit)
;

create table user
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
avatar_url varchar(255) null,
email varchar(255) null,
lastname varchar(255) null,
latitude double null,
location_name varchar(255) null,
longitude double null,
name varchar(255) null,
password varchar(255) null,
password_length int null,
position varchar(255) null,
profile_url varchar(255) null,
system char default 'F' null,
user_type varchar(255) not null,
verification_key varchar(255) null,
verified char not null,
organization bigint null,
constraint FK_a7krvkolmrchxwj22txuhlhj
foreign key (organization) references prosolo3.organization (id)
)
;

create index FK_a7krvkolmrchxwj22txuhlhj
	on user (organization)
;

alter table activity1
add constraint FK_51vt5mwt287fcjsoap7vsm5e2
		foreign key (created_by) references prosolo3.user (id)
;

alter table activity_discussion_participant
add constraint FK_klimyuls8rsoqt7v9ce5gqts9
		foreign key (participant) references prosolo3.user (id)
;

alter table annotation1
add constraint FK_qg56tf64cpvwkc5p2q9bjoape
		foreign key (maker) references prosolo3.user (id)
;

alter table announcement
add constraint FK_i5ssis4ksi4j8jsbb9xga1odi
		foreign key (created_by) references prosolo3.user (id)
;

alter table comment1
add constraint FK_cnb1hy7io12vb2fbi1jdmyalw
		foreign key (user) references prosolo3.user (id)
;

alter table competence1
add constraint FK_j0jn9c30xitfotmamc9181go5
		foreign key (created_by) references prosolo3.user (id)
;

alter table competence_assessment
add constraint FK_lbo2bjanrdpkwedf9p6q6o248
		foreign key (assessor) references prosolo3.user (id)
;

alter table competence_assessment
add constraint FK_tiy256v79b1c8vfiest7m8piw
		foreign key (student) references prosolo3.user (id)
;

alter table competence_assessment_discussion_participant
add constraint FK_tk9bucqcwbukxgjkkckysawf5
		foreign key (participant) references prosolo3.user (id)
;

alter table competence_bookmark
add constraint FK_7fwnajpqa9j4dfaciibethkll
		foreign key (user) references prosolo3.user (id)
;

alter table credential1
add constraint FK_gwgwt3bm30pwx6lxiukyx1o03
		foreign key (created_by) references prosolo3.user (id)
;

alter table credential_assessment
add constraint FK_64x1nbncerttcr6ukwn1i10yy
		foreign key (assessor) references prosolo3.user (id)
;

alter table credential_assessment
add constraint FK_o0soi8jus7un1g5yqyfrpo3rs
		foreign key (student) references prosolo3.user (id)
;

alter table credential_assessment_discussion_participant
add constraint FK_safwo8xgq1h61ttmdd9kob4hp
		foreign key (participant) references prosolo3.user (id)
;

alter table credential_bookmark
add constraint FK_n5fkjwxicertabyarbvju43c3
		foreign key (user) references prosolo3.user (id)
;

alter table credential_instructor
add constraint FK_5he8qwqjrj5oitt390xv1c363
		foreign key (user) references prosolo3.user (id)
;

alter table feed_entry
add constraint FK_cgxvubmhf09musstvw2u00y67
		foreign key (maker) references prosolo3.user (id)
;

alter table feed_entry
add constraint FK_5mympaondc7gotjymqgd05l4i
		foreign key (subscribed_user) references prosolo3.user (id)
;

alter table feeds_digest
add constraint FK_j4nanu4eoo6lobqy4v64tegmf
		foreign key (feeds_subscriber) references prosolo3.user (id)
;

alter table followed_entity
add constraint FK_9i2cqb9qu9aomuu5ju2yrm6f9
		foreign key (user) references prosolo3.user (id)
;

alter table followed_entity
add constraint FK_4eglof45iulhoj8j2qudvbuxm
		foreign key (followed_user) references prosolo3.user (id)
;

alter table learning_evidence
add constraint FK_te80g9rqpmjnlpmff23h0we5x
		foreign key (user) references prosolo3.user (id)
;

alter table lti_tool
add constraint FK_n8iw9ueffp6ceo31pnuensikq
		foreign key (created_by) references prosolo3.user (id)
;

alter table lti_user
add constraint FK_hxb07pfijibeqr1mm8qnnam0i
		foreign key (user) references prosolo3.user (id)
;

alter table message_participant
add constraint FK_nt68vh666kew9wsx344d2tej8
		foreign key (participant) references prosolo3.user (id)
;

alter table message_thread
add constraint FK_owblue8e7depfe80u0tut6vnp
		foreign key (creator) references prosolo3.user (id)
;

alter table notification1
add constraint FK_8ffsbfqrwx0l4fcwtir4eogs8
		foreign key (actor) references prosolo3.user (id)
;

alter table notification1
add constraint FK_eu8xhjr832wpkchfxn2a4ne8f
		foreign key (receiver) references prosolo3.user (id)
;

alter table notification_settings
add constraint FK_ajh6l15bdar6g8o7d01b52j3t
		foreign key (user) references prosolo3.user (id)
;

alter table oauth_access_token
add constraint FK_qwnyxe99obn5c7jamrh80tojn
		foreign key (user) references prosolo3.user (id)
;

alter table observation
add constraint FK_f238497jjp2hx63imjj1ndnbv
		foreign key (created_by) references prosolo3.user (id)
;

alter table observation
add constraint FK_owstmit5g7fnsggixn5tmfmd7
		foreign key (created_for) references prosolo3.user (id)
;

alter table openidaccount
add constraint FK_mrch25e7jcip15grmcp54039i
		foreign key (user) references prosolo3.user (id)
;

alter table reset_key
add constraint FK_fxv54xs4m5y1tddmi2errcesu
		foreign key (user) references prosolo3.user (id)
;

alter table rubric
add constraint FK_6peou3uwnq3v5xoirhfg4buwh
		foreign key (creator) references prosolo3.user (id)
;

alter table seen_announcement
add constraint FK_8xib2pv1vo1dkrj7mwkc7stt0
		foreign key (user) references prosolo3.user (id)
;

alter table social_activity1
add constraint FK_369a166cd064b6ju3laifxqh6
		foreign key (actor) references prosolo3.user (id)
;

alter table social_activity_config
add constraint FK_p4ouuukgvnn6j8to9pc5xnlq4
		foreign key (user) references prosolo3.user (id)
;

alter table target_competence1
add constraint FK_62p0o7vw29vwnqc58g1hu2vai
		foreign key (user) references prosolo3.user (id)
;

alter table target_credential1
add constraint FK_76pg8xnfhsda0bvvq7wvwl5xp
		foreign key (user) references prosolo3.user (id)
;

alter table thread_participant
add constraint FK_ks01qd3o7158kirgyfpkw6cbb
		foreign key (user) references prosolo3.user (id)
;

alter table unit_role_membership
add constraint FK_oiefemc5sijcc7vop0b97lpt6
		foreign key (user) references prosolo3.user (id)
;

create table user_group
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
default_group char default 'F' null,
join_url_active char default 'F' null,
join_url_password varchar(255) null,
name varchar(255) null,
unit bigint null,
constraint FK_ddfpocbhgcpv8uhvcuf52dx5a
foreign key (unit) references prosolo3.unit (id)
)
;

create index FK_ddfpocbhgcpv8uhvcuf52dx5a
	on user_group (unit)
;

alter table competence_user_group
add constraint FK_4b5s1r5gcdytwdbp2pr4rn4v5
		foreign key (user_group) references prosolo3.user_group (id)
;

alter table credential_user_group
add constraint FK_m5cvxlf67t55is1xw2gxfar0i
		foreign key (user_group) references prosolo3.user_group (id)
;

alter table lti_tool
add constraint FK_64dtvy9vcghm1b7cv0ymsgg4p
		foreign key (user_group) references prosolo3.user_group (id)
;

create table user_group_user
(
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
user_group bigint not null,
user bigint not null,
constraint UK_c63cjs9t3jl6nrf3asi7iej52
unique (user, user_group),
constraint FK_iifontadv5293tvuvf7r9e9u8
foreign key (user_group) references prosolo3.user_group (id),
constraint FK_kqb30gm40pn64yo6sgh0jgpcf
foreign key (user) references prosolo3.user (id)
)
;

create index FK_iifontadv5293tvuvf7r9e9u8
	on user_group_user (user_group)
;

create table user_preference
(
	dtype varchar(31) not null,
	id bigint not null
		primary key,
	created datetime null,
	deleted char default 'F' null,
	description longtext null,
	title varchar(255) null,
update_period varchar(255) not null,
user bigint null,
personal_blog_source bigint null,
constraint FK_lubkkfg4oouxyab4bblgr0j2e
foreign key (user) references prosolo3.user (id),
constraint FK_oskk4hx06767511e5ar5wpdo1
foreign key (personal_blog_source) references prosolo3.feed_source (id)
)
;

create index FK_lubkkfg4oouxyab4bblgr0j2e
	on user_preference (user)
;

create index FK_oskk4hx06767511e5ar5wpdo1
	on user_preference (personal_blog_source)
;

create table user_preference_subscribed_rss_sources
(
	user_preference bigint not null,
	subscribed_rss_sources bigint not null,
	constraint UK_k33l3f2g6mo8kpnc0laxm8dxf
		unique (subscribed_rss_sources),
constraint FK_k1w7aarxe3y1g47f1k9qjh5qb
foreign key (user_preference) references prosolo3.user_preference (id),
constraint FK_k33l3f2g6mo8kpnc0laxm8dxf
foreign key (subscribed_rss_sources) references prosolo3.feed_source (id)
)
;

create index FK_k1w7aarxe3y1g47f1k9qjh5qb
	on user_preference_subscribed_rss_sources (user_preference)
;

create table user_settings
(
	id bigint not null
		primary key,
	activity_wall_settings bigint null,
	locale_settings bigint null,
	terms_of_use bigint null,
	constraint FK_e0ji1xk8xkrg1ee92jyuhl5s4
		foreign key (id) references prosolo3.user (id),
constraint FK_phjwu7o2219iuuh2v53ombw6w
foreign key (activity_wall_settings) references prosolo3.activity_wall_settings (id),
constraint FK_o3slgonymb8ch3md0m5anhlb8
foreign key (locale_settings) references prosolo3.locale_settings (id),
constraint FK_fdwr1xr3v58sye7ukapj4k3pa
foreign key (terms_of_use) references prosolo3.terms_of_use (id)
)
;

create index FK_fdwr1xr3v58sye7ukapj4k3pa
	on user_settings (terms_of_use)
;

create index FK_o3slgonymb8ch3md0m5anhlb8
	on user_settings (locale_settings)
;

create index FK_phjwu7o2219iuuh2v53ombw6w
	on user_settings (activity_wall_settings)
;

create table user_settings_pages_tutorial_played
(
	user_settings bigint not null,
	pages_tutorial_played varchar(255) null,
constraint FK_rdbdifanhafj621wt7cd8iurc
foreign key (user_settings) references prosolo3.user_settings (id)
)
;

create index FK_rdbdifanhafj621wt7cd8iurc
	on user_settings_pages_tutorial_played (user_settings)
;

create table user_social_networks
(
	id bigint not null
		primary key,
	user bigint null,
	constraint FK_eriyguekd0ayq7n6njjy86qp
		foreign key (user) references prosolo3.user (id)
)
;

create index FK_eriyguekd0ayq7n6njjy86qp
	on user_social_networks (user)
;

create table user_social_networks_social_network_accounts
(
	user_social_networks bigint not null,
	social_network_accounts bigint not null,
	primary key (user_social_networks, social_network_accounts),
constraint UK_cm6y96m10clu35b3ow94gi5ln
unique (social_network_accounts),
constraint FK_8qwchc8e5iamlaaysbpg3p7su
foreign key (user_social_networks) references prosolo3.user_social_networks (id),
constraint FK_cm6y96m10clu35b3ow94gi5ln
foreign key (social_network_accounts) references prosolo3.social_network_account (id)
)
;

create table user_topic_preference_preferred_hashtags_tag
(
	user_preference bigint not null,
	preferred_hashtags bigint not null,
	primary key (user_preference, preferred_hashtags),
constraint FK_ftoj3377h5tkd5url4ajuqdkt
foreign key (user_preference) references prosolo3.user_preference (id),
constraint FK_c2yhsp4xled49t7brdhsapk24
foreign key (preferred_hashtags) references prosolo3.tag (id)
)
;

create index FK_c2yhsp4xled49t7brdhsapk24
	on user_topic_preference_preferred_hashtags_tag (preferred_hashtags)
;

create table user_topic_preference_preferred_keywords_tag
(
	user_preference bigint not null,
	preferred_keywords bigint not null,
	primary key (user_preference, preferred_keywords),
constraint FK_deretkpd06iv6p27971yv48vt
foreign key (user_preference) references prosolo3.user_preference (id),
constraint FK_486ac11t2tsvkm3cj1qjn61pu
foreign key (preferred_keywords) references prosolo3.tag (id)
)
;

create index FK_486ac11t2tsvkm3cj1qjn61pu
	on user_topic_preference_preferred_keywords_tag (preferred_keywords)
;

create table user_user_role
(
	user bigint not null,
	roles bigint not null,
	primary key (user, roles),
constraint FK_1e97vv9xu9fx2kaeivgbh1jdx
foreign key (user) references prosolo3.user (id),
constraint FK_9uoa85no4a82ukddaycpt17f
foreign key (roles) references prosolo3.role (id)
)
;

create index FK_9uoa85no4a82ukddaycpt17f
	on user_user_role (roles)
;

