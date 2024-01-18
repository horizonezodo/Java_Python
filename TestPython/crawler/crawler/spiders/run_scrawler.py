from scrapy.crawler import CrawlerProcess
import json
import os
import sys

sys.path.append('E:\\python_leaning\\TestPython\\crawler')
from crawlerData import CrawlerSpider
def run_crawler():
    process = CrawlerProcess({
        'USER_AGENT': 'Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)',
        'FEED_FORMAT': 'json',  # Định dạng xuất
        'FEED_URI': 'E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json'  # Đường dẫn tệp xuất
    })
    process.crawl(CrawlerSpider)
    process.start()

def load_json_with_unicode(file_path):
    with open(file_path, 'r', encoding='utf-8') as json_file:
        data = json_file.read()
        return json.loads(data)


def dump_json(data, file_path):
    with open(file_path, 'w', encoding='utf-8') as json_file:
        json.dump(data, json_file, ensure_ascii=False, indent=2)

def check_file_exists(file_path):
    return os.path.exists(file_path)

if __name__ == "__main__":
    if check_file_exists('E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json'):
        os.remove('E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json')
    run_crawler()
    json_file_path = 'E:\\python_leaning\\TestPython\\crawler\\crawler\\spiders\\output.json'
    data = load_json_with_unicode(json_file_path)
    output_file_path = 'E:\\Crawler\\data\\encode.json'
    dump_json(data, output_file_path)
