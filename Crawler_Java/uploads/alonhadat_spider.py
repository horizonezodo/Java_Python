from scrapy import Spider
from scrapy.selector import Selector
from crawler.items import CrawlerItem

class firstSpider(Spider): 
   name = "alonhadat" 
   allowed_domains = ["alonhadat.com.vn"] 
   
   start_urls = [ 
      "https://alonhadat.com.vn/nha-dat/can-ban/nha-dat/1/ha-noi.html"
   ]  
   def parse(self, response): 
        filename = response.url.split("/")[-2] + '.html' 
        with open(filename, 'wb') as f: 
            f.write(response.body)
        questions = Selector(response).xpath('//div[@class="content-items"]/div[@class="content-item"]')
		

        for question in questions:
            item = CrawlerItem()

            item['title'] = question.xpath(
                'div/div[@class="ct_title"]/a/text()').extract_first()
            item['description'] = question.xpath(
                'div[@class="text"]/div[@class="ct_brief"]/text()').extract_first()
            item['sdt'] = question.xpath(
                'div[@class="cmt-command"]/span/text()').extract_first()

            yield item