# write_and_read_csv.py
import csv
import json
import sys

class WebsiteDescription:
    def __init__(self, id, title, description, sdt, website_id):
        self.id = id
        self.title = title
        self.description = description
        self.sdt = sdt
        self.website_id = website_id

def write_to_csv(file_path, data, website_id):
    with open(file_path, 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerow(['id', 'title', 'description', 'sdt', 'website_id'])
        for web in data:
            writer.writerow([web['id'], web['title'], web['description'], web['sdt'], website_id])

def read_csv(file_path):
    data = []
    with open(file_path, 'r') as file:
        reader = csv.reader(file)
        header = next(reader)  # Skip the header
        for row in reader:
            websiteDescription = WebsiteDescription(row[0], row[1], row[2], row[3], row[4])
            data.append(websiteDescription.__dict__)
    return json.dumps(data)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print('Usage: python write_and_read_csv.py <csv_file_path> <website_id>')
        sys.exit(1)

    csv_file_path = sys.argv[1]
    website_id = sys.argv[2]

    data_to_write = [
        {'id': '1', 'title': 'Website Google', 'description': 'Most popular user', 'sdt': '+841234567'},
        {'id': '2', 'title': 'Website Google 2', 'description': 'Most popular user', 'sdt': '+841234567'},
        {'id': '3', 'title': 'Website Google 3', 'description': 'Most popular user', 'sdt': '+841234567'},
        {'id': '4', 'title': 'Website Youtube', 'description': 'Most popular user', 'sdt': '+841234567'}
    ]
    write_to_csv(csv_file_path, data_to_write, website_id)
    rs = read_csv(csv_file_path)
    print(rs)
