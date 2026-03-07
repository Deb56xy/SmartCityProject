import joblib
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline

# Load dataset
df = pd.read_csv("data/complaint_category.csv")

# Keep only valid categories (exclude Other for training)
df = df[df["category"] != "Other"]

x = df["description"]
y = df["category"]

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

model.fit(x, y)

# Save model
joblib.dump(model, "models/category_model.pkl")
print("Category model trained and saved")