import os
import pandas as pd
import joblib
from sklearn.pipeline import Pipeline
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression

BASE_FAKE = "data/complaint_validity.csv"
INCR_FAKE = "data/incremental_train.csv"

BASE_PRIORITY = "data/complaint_priority.csv"
INCR_PRIORITY = "data/incremental_train_priority.csv"

BASE_CATEGORY = "data/complaint_category.csv"
INCR_CATEGORY = "data/incremental_train_category.csv"

def train_model(data, target_col, model_path):
    x = data["description"]
    y = data[target_col]

    # NLP + ML Pipeline
    model = Pipeline([
        ("tfidf", TfidfVectorizer(
            lowercase=True,
            stop_words="english",
            ngram_range=(1, 2),
            max_features=5000
        )),
        ("classifier", LogisticRegression(
            max_iter=1000,
            class_weight="balanced"
        ))
    ])

    # Train model
    model.fit(x, y)
    joblib.dump(model, model_path)

def retrain_models():
    print("Retraining ML models")

    # ---- FAKE MODEL ----
    fake_base = pd.read_csv(BASE_FAKE)
    fake_incr = pd.read_csv(INCR_FAKE) if os.path.exists(INCR_FAKE) else None
    fake_data = pd.concat([fake_base, fake_incr]) if fake_incr is not None else fake_base

    train_model(fake_data, "label", "models/fake_detector.pkl")
    print("Fake detection model retrained")

    # ---- PRIORITY MODEL ----
    pri_base = pd.read_csv(BASE_PRIORITY)
    pri_incr = pd.read_csv(INCR_PRIORITY) if os.path.exists(INCR_PRIORITY) else None
    pri_data = pd.concat([pri_base, pri_incr]) if pri_incr is not None else pri_base

    train_model(pri_data, "priority", "models/priority_model.pkl")
    print("Priority model retrained")

    # ---- CATEGORY MODEL ----
    cat_base = pd.read_csv(BASE_CATEGORY)
    cat_incr = pd.read_csv(INCR_CATEGORY) if os.path.exists(INCR_CATEGORY) else None
    cat_data = pd.concat([cat_base, cat_incr]) if cat_incr is not None else cat_base

    train_model(cat_data, "category", "models/category_model.pkl")
    print("Category model retrained")