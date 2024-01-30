Bước 1 : Check Java version của dự án, kiểm tra Java version compiler nếu không cùng 1 phiên bản thì đưa về cùng 1 phiên bản  .
	Mở sring tool suit 4 lên -> mở project viết bằng intelliJ -> chuột phải vào file -> chọn properties -> chon java compiler
	
Bước 2 : Check Java version môi trường.
Bước 3: Nếu Java version của dự án nó khác với Java version môi trường thì chuyển phiên bản Java của dự án về cùng với với java version của môi trường
Bước 4: Kiểm tra version của tomcat => check lại dự án xem có vấn đề với tomcat không ( ví dụ tomcat 9 có thể chịu được java 8, 
											java 11 và java 17 nhưng nếu sử dụng java 11 và java 
											17 nếu sử dụng spring boot 3 thì kiểm tra nếu @Entity 
											sử dụng jarkata thì hạ phiên bản spring boot về 2. và sử dụng javax)
Bước 5 : Mở sring tool suit 4 lên -> mở project viết bằng intelliJ -> chuột phải vào file -> chọn properties -> chon java build path -> chọn order and export
	-> tích chọn maven dependencies -> chọn apply and close 
Bước 6: Kiểm tra xem các annotation có vấn đề gì không ( annotation trong java build path ) nếu có thì update lại maven cho dự án
Bước 7: Export file war bằng sts4
Bước 8: Coppy file war lên webapps 
Bước 9: Khởi chạy tomcat -> done

Trong quá trình deploy lên tomcat thì nếu sử dụng đường dẫn tuyệt đối thì check lại các đường dẫn
Nếu deploy lên tomcat thì nên check lại những phương thức có sử dụng cmd trong windown và bin/bash trong ubuntu để kiểm tra lại xem các method đúng hay không
