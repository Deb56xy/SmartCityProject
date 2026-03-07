import atexit

import requests
from apscheduler.schedulers.background import BackgroundScheduler

from auto_retrain import retrain_models
from update_training_data import update_csv

RELOAD_URL = "http://localhost:5000/reload-models"

def job():
    print("ML scheduler started")
    update_csv()
    retrain_models()
    requests.post(RELOAD_URL)
    print("App model updated live")

def start_scheduler():
    # Run every 2 mins
    scheduler = BackgroundScheduler()
    scheduler.add_job(job, "interval", minutes=2)
    scheduler.start()
    atexit.register(lambda: scheduler.shutdown())