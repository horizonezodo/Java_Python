import subprocess
import sys

def run_commands(json_file_path):
    # Thay đổi thư mục làm việc
    subprocess.run(["cd", "/d", "E:\\python_leaning\\TestPython\\crawler\\venv"], shell=True)

    # Kích hoạt môi trường ảo
    subprocess.run(["E:\\python_leaning\\TestPython\\crawler\\venv\\Scripts\\activate"], shell=True)

    # Thay đổi thư mục làm việc
    subprocess.run(["cd", "E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\"], shell=True)

    # Chạy tệp python với đường dẫn JSON được truyền vào từ dòng lệnh
    subprocess.run(["python", "E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\run_scrawler.py", json_file_path], shell=True)

if __name__ == "__main__":
    # Lấy đường dẫn tệp JSON từ dòng lệnh
    json_file_path = sys.argv[1]
    #json_file_path = "E:\\uploads\\alonhadat_spider.py"
    run_commands(json_file_path)
