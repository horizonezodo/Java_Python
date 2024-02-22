import scrapy


def parse_item_page(response):
    house_img = response.css('ul#ContentPlaceHolder1_ctl00_viewImage1_divLi li a::attr(href)').getall()
    for item in house_img:
        item = "https://nhadat24h.net" + item
    yield{
        'url': response.url,
        'title':  response.css('div.header h1::text').get(),
        'seller_user': response.css('label.fullname a::text').get(),
        'seller_phone': response.css('div.info label::text').getall()[0],
        'detail': response.css('div.dv-txt-mt p::text').getall(),
        'price': response.css('label#ContentPlaceHolder1_ctl00_lbGiaDienTich label.strong1::text').get(),
        'square': response.css('label#ContentPlaceHolder1_ctl00_lbGiaDienTich label.strong2::text').get(),
        'house_image': house_img,
    }


class BdsSpider(scrapy.Spider):
    name = "bds123"
    allowed_domains = ['bds123.vn']
    start_urls = ['https://bds123.vn/nha-dat-ban-ha-noi.html']

    custom_settings = {
        'FEEDS': {
            'E:/output.json': {'format': 'json', 'overwrite': True}
        }
    }

    def parse(self, response):
        listbds = response.css('ul.post-listing li.item')
        for bds in listbds:
            relative_url = bds.css('ul.post-listing li.item a::attr(href)').get()
            item_url = 'https://bds123.vn' + relative_url
            yield response.follow(item_url, callback=parse_item_page)

        list_page = response.css('ul.pagination li.page-item a::attr(href)').getall()
        current_page = response.css('div.dv-pt-ttcm li.active a::attr(href)').get()
        print("=======================================")
        print(current_page)
        print("==========================================")
        index_curren_page = list_page.index(current_page)
        print("=========================================== danh sach list")
        print(list_page)
        print("===========================================================")
        print("====================================================================")
        print(index_curren_page)
        print("===============================================================================")

        if index_curren_page < len(list_page):
            next_page = list_page[index_curren_page + 1]
            next_page_url = 'https://nhadat24h.net' + next_page
            yield response.follow(next_page_url, callback=self.parse)
        elif index_curren_page == len(list_page):
            next_page = list_page[index_curren_page]
            next_page_url = 'https://nhadat24h.net' + next_page
            yield response.follow(next_page_url, callback=self.parse)
