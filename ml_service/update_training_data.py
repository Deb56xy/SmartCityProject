import os

import pandas as pd

from fetch_reviewed_complaints import fetch_reviewed

INCREMENTAL_CSV = "data/incremental_train.csv"
PRIORITY_INCREMENTAL_CSV = "data/incremental_train_priority.csv"
CATEGORY_INCREMENTAL_CSV = "data/incremental_train_category.csv"

def append_to_csv(path, new_data):
    if os.path.exists(path):
        existing = pd.read_csv(path)
        combined = pd.concat([existing, new_data]).drop_duplicates()
    else:
        combined = new_data
    combined.to_csv(path, index=False)

def update_csv():
    print("Fetching reviewed complaints from DB")
    df = fetch_reviewed()

    if df.empty:
        print("No new reviewed complaints")
        return

    # Fake detection data
    fake_df = (df[["description", "label"]]
               .dropna(subset=["label"])
               .query("label != ''"))


    # Priority data
    priority_df = (df[["description", "priority"]]
                .dropna(subset=["priority"])
                .query("priority != ''"))

    # Category data
    category_df = ((df[["description", "category"]])
                .dropna(subset=["category"])
                .query("category != ''"))

    append_to_csv(INCREMENTAL_CSV, fake_df)
    append_to_csv(PRIORITY_INCREMENTAL_CSV, priority_df)
    append_to_csv(CATEGORY_INCREMENTAL_CSV, category_df)
    print(f"Incremental data updated ({len(df)} rows)")