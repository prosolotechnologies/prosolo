SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE annotation;
DROP TABLE badge;
DROP TABLE competence_activity;
DROP TABLE course;
DROP TABLE course_basedon_course;
DROP TABLE course_blogs;
DROP TABLE course_competence;
DROP TABLE course_enrollment;
DROP TABLE course_enrollment_added_competences_course_competence;
DROP TABLE course_excluded_feed_sources;
DROP TABLE course_hashtags;
DROP TABLE course_instructor;
DROP TABLE course_portfolio;
DROP TABLE course_portfolio_enrollments_course_enrollment;
DROP TABLE course_tags;
DROP TABLE completed_resource;
DROP TABLE evaluation;
DROP TABLE evaluation_badges;
DROP TABLE evaluation_submission;
DROP TABLE evaluation_submission_evaluations;
DROP TABLE event_reminder_guests;
DROP TABLE featured_news;
DROP TABLE featured_news_inbox;
DROP TABLE featured_news_inbox_user_featured_news;
DROP TABLE learning_goal_basedon_learning_goal;
DROP TABLE node;
DROP TABLE node_corequisites;
DROP TABLE node_hashtags;
DROP TABLE node_outcomes;
DROP TABLE node_prerequisites;
DROP TABLE node_tags;
DROP TABLE node_target_activities;
DROP TABLE notification;
DROP TABLE personal_calendar;
DROP TABLE personal_calendar_notifications;
DROP TABLE personal_calendar_reminders;
DROP TABLE portfolio;
DROP TABLE portfolio_external_credit_competences;
DROP TABLE portfolio_external_credit_target_activities;
DROP TABLE portfolio_portfolio_achieved_competence;
DROP TABLE portfolio_portfolio_completed_goal;
DROP TABLE portfolio_portfolio_external_credits;
DROP TABLE post;
DROP TABLE post_hashtags;
DROP TABLE post_mentioned_users;
DROP TABLE recommendation;
DROP TABLE reminder;
DROP TABLE request;
DROP TABLE rich_content;
DROP TABLE social_activity;
DROP TABLE social_activity_hashtags;
DROP TABLE social_activity_comments;
DROP TABLE social_activity_notification;
DROP TABLE social_activity_notification_sub_views;
DROP TABLE social_stream_sub_view;
DROP TABLE social_stream_sub_view_hashtags;
DROP TABLE social_stream_sub_view_related_resources;
DROP TABLE user_featured_news;
DROP TABLE user_learning_goals;
DROP TABLE user_learning_goal_target_competence;
DROP TABLE user_notification_actions;
ALTER TABLE followed_entity DROP FOREIGN KEY FK_e621cferbujqxfxjau0o4mfq6 ;
ALTER TABLE followed_entity DROP INDEX FK_e621cferbujqxfxjau0o4mfq6;
ALTER TABLE followed_entity DROP COLUMN followed_node;
SET FOREIGN_KEY_CHECKS = 1;