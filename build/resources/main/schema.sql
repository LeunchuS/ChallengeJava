CREATE TABLE TRANSACTION (
                             transaction_id BIGINT PRIMARY KEY,
                             account_id BIGINT NOT NULL,
                             amount DECIMAL(19,2) NOT NULL,
                             type VARCHAR(20) NOT NULL,
                             created_at TIMESTAMP NOT NULL
);

CREATE SEQUENCE PROCESS_SEQ START WITH 1 INCREMENT BY 1;
CREATE TABLE FILE_PROCESS (
                             file_process_id BIGINT PRIMARY KEY,
                             total_records INT NOT NULL,
                             processed_records INT NOT NULL,
                             error_records INT NOT NULL,
                             duplicated_records INT NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             status VARCHAR(20) NOT NULL
);