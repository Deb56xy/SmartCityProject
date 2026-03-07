import threading

import joblib
from flask import Flask, request, jsonify
import psycopg2

from schedular import start_scheduler

app = Flask(__name__)

FAKE_MODEL_PATH = "models/fake_detector.pkl"
PRIORITY_MODEL_PATH = "models/priority_model.pkl"
CATEGORY_MODEL_PATH = "models/category_model.pkl"

model_lock = threading.Lock()

fake_model = joblib.load(FAKE_MODEL_PATH)
priority_model = joblib.load(PRIORITY_MODEL_PATH)
category_model = joblib.load(CATEGORY_MODEL_PATH)

print("Models loaded at startup")
start_scheduler()

def reload_models():
    global fake_model, priority_model, category_model
    with model_lock:
        fake_model = joblib.load(FAKE_MODEL_PATH)
        priority_model = joblib.load(PRIORITY_MODEL_PATH)
        category_model = joblib.load(CATEGORY_MODEL_PATH)
        print("Models reloaded successfully")
    return jsonify({"status": "models reloaded"}), 200

@app.route("/predict", methods=["POST"])
def predict_fake():
    data = request.get_json()
    description = data.get("description", "").strip()

    if not description:
        return jsonify({
            "prediction": "FAKE",
            "confidence": 1.0,
            "reason": "Empty description"
        })

    with model_lock:
        probs = fake_model.predict_proba([description])[0]
        classes = fake_model.classes_

    idx = probs.argmax()
    return jsonify({
        "prediction": classes[idx],
        "confidence": round(float(probs[idx]), 2)
    })

@app.route("/predict/priority", methods=["POST"])
def predict_priority():
    data = request.get_json()
    description = data.get("description", "").strip()

    if not description:
        return jsonify({
            "priority": "LOW",
            "confidence": 1.0,
            "reason": "Empty description"
        })

    with model_lock:
        probs = priority_model.predict_proba([description])[0]
        classes = priority_model.classes_

    idx = probs.argmax()
    return jsonify({
        "priority": classes[idx],
        "confidence": round(float(probs[idx]), 2)
    })

@app.route("/predict/category", methods=["POST"])
def predict_category():
    data = request.get_json()
    description = data.get("description", "").strip()
    if not description:
        return jsonify({
            "category": "OTHER",
            "confidence": 1.0,
            "reason": "Empty description"
        })

    with model_lock:
        probs = category_model.predict_proba([description])[0]
        classes = category_model.classes_

    max_index = probs.argmax()
    predicted_category = classes[max_index]
    confidence = probs[max_index]
    return jsonify({
        "category": predicted_category,
        "confidence": round(float(confidence), 2)
    })

@app.route("/reload-models", methods=["POST"])
def reload_endpoint():
    reload_models()
    return jsonify({"status": "models reloaded"})

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, use_reloader=False)