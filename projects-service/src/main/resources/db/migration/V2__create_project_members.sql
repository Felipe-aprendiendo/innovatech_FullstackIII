CREATE TABLE project_members (
    project_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    PRIMARY KEY (project_id, member_id),
    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);
