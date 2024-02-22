import scrapy
from testProject.items import TestItem


def parse_item_page(response):
    test_item = TestItem()
    test_item['url']= response.url
    test_item['title']=response.css('div.property div.title h1::text').get()
    test_item['seller_user']= response.css('div.content div.name::text').get()
    test_item['seller_phone']= response.css('div.fone a::text').get()
    test_item['detail']= response.css('div.property div.detail::text').get()
    test_item['price']= response.css('div.moreinfor span.value::text').get()
    test_item['square']= response.css('span.square span.value::text').get()
    test_item['house_image']= response.css('div.imageview img::attr(src)').get()
    yield test_item


class BdsSpider(scrapy.Spider):
    name = "batdongsanspider"
    allowed_domains = ['alonhadat.com.vn']
    start_urls = ['https://alonhadat.com.vn/nha-dat/can-ban/nha-dat/1/ha-noi.html']

    custom_settings = {
        'FEEDS' : {
            'E:/output.json':{'format':'json', 'overwrite':True}
        }
    }

    def parse(self, response):
        listbds = response.css('div.content-item')
        for bds in listbds:
            relative_url = bds.css('div.ct_title a::attr(href)').get()
            item_url = 'https://alonhadat.com.vn' + relative_url
            yield response.follow(item_url, callback=parse_item_page)

        list_page = response.css('div.page a ::attr(href)').getall()
        print(list_page)
        index_i = list_page.index('#')
        print("====================================================================")
        print(index_i)
        print("===============================================================================")
        if index_i < len(list_page):
            next_page = list_page[index_i + 1]
            next_page_url = 'https://alonhadat.com.vn/' + next_page
            yield response.follow(next_page_url, callback=self.parse)
        elif index_i == len(list_page):
            next_page = list_page[index_i]
            next_page_url = 'https://alonhadat.com.vn/' + next_page
            yield response.follow(next_page_url, callback=self.parse)
