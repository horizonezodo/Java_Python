import pyttsx3
import datetime
import speech_recognition as sr
import webbrowser as wb
import os

friday = pyttsx3.init()
voice = friday.getProperty('voices')
friday.setProperty('voice', voice[1].id)

def speak(audio):
    print('F.R.I.D.A.Y.: ' + audio)
    friday.say(audio)
    friday.runAndWait()

def welcome():
    hour = datetime.datetime.now().hour
    if hour >= 6 and hour < 12:
        speak('Good Morning sir')
    elif hour >= 12 and hour < 18:
        speak('Good Afternoon sir')
    elif hour >= 18 and hour < 24:
        speak('Good Night sir')
    speak('How can i help you')

def command(file_path):
    c = sr.Recognizer()
    with sr.AudioFile(file_path) as source:
        c.pause_threshold = 2
        audio = c.record(source)

# def command():
#     c = sr.Recognizer()
#     with sr.Microphone() as source:
#         c.pause_threshold = 2
#         audio = c.listen(source)
    try:
        query = c.recognize_google(audio, language='en')
        print('Lou: ' + query)
    except sr.UnknownValueError:
        print("Please repeat or typing the command")
        query = str(input('Your order is: '))
    return query

if __name__ == "__main__":
    welcome()
    while True:
        file_path = "C:\\Users\\dev\\PycharmProjects\\py1\\open video.wav"
        query = command(file_path).lower()

        if "youtube" in query:
            speak("What should I search boss?")
            search = command().lower()
            url = f'https://www.youtube.com/search?q={search}'
            wb.get().open(url)
            speak(f'Here is your {search} on youtube')
        elif "google" in query:
            speak("What should I search boss?")
            file_path = "C:\\Users\\dev\\PycharmProjects\\py1\\Ironman2.wav"
            search = command(file_path).lower()
            url = f'https://www.google.com/search?q={search}'
            wb.get().open(url)
            speak(f'Here is your {search} on google')
        elif "open video" in query:
            meme = r"C:\\Users\\dev\\PycharmProjects\\py1\\vn-11110103-6ke14-lmhknrq0tdjzaf.default.mp4"
            os.startfile(meme)
        else:
            speak("Friday is quitting sir. Goodbye boss")
            quit()