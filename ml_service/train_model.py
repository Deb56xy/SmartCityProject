import pandas as pd
import joblib

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline

# Load Dataset
data = pd.read_csv("data/complaint_validity.csv")

X = data["description"]
y = data["label"]

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
model.fit(X, y)

# Save trained model
joblib.dump(model, "models/fake_detector.pkl")
print("Fake complaint detection model trained and saved")