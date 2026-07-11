CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    original_content TEXT,
    optimized_content TEXT,
    job_jd TEXT,
    match_score INT,
    match_report TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS interview_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_name VARCHAR(100) NOT NULL,
    question TEXT NOT NULL,
    user_answer TEXT,
    ai_comment TEXT,
    standard_answer TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ai_invoke_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    scene VARCHAR(50) NOT NULL,
    model_name VARCHAR(100),
    duration_ms BIGINT,
    status VARCHAR(20) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_ai_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    base_url VARCHAR(500) NOT NULL,
    model VARCHAR(120) NOT NULL,
    api_key_cipher TEXT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
