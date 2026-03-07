import pandas as pd
import psycopg2

def fetch_reviewed():
    print("Creating DB connection")
    conn = psycopg2.connect(
        host="postgres",
        port=5432,
        dbname="smart_city_db",
        user="postgres",
        password="Barasat@100",
        connect_timeout=5
    )
    print("Connection established")

    query = """
        SELECT c.description, 
            CASE
                WHEN sl.new_status = 'REJECTED' THEN 'FAKE'
                WHEN sl.new_status IN ('NEW', 'RESOLVED', 'IN_PROGRESS') THEN 'GENUINE'
            END AS label,
            sl.new_priority AS priority,
            dept.name AS category
        FROM status_logs sl JOIN complaints c ON sl.complaint_id = c.id
            LEFT JOIN departments dept ON sl.new_department = dept.id
        WHERE(
            sl.old_status = 'UNDER_REVIEW'
            AND sl.new_status IN ('REJECTED', 'NEW', 'IN_PROGRESS', 'RESOLVED')) 
            OR (sl.new_priority IS NOT NULL) OR (sl.new_department IS NOT NULL);
    """

    df = pd.read_sql(query, conn)
    print(f"Fetched {len(df)} rows")

    conn.close()
    return df
