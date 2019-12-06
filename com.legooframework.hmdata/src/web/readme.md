# 合墨交易接口适配层说明

---
## 1、短信绑卡鉴权接口

**请求URL：** 
- ` http://localhost:8080/hmdata/api/bankCardSign/action.json `

**请求参数：**
  参见原文档
  
**请求方式：**
  - POST 

**Example（采用POSTMAN 软件测试OK）**

``` 
  {
  	"version":"1.0",
  	"merchant_no":"1018",
  	"sign_order_no":"AP2I1110182122312212212255",
  	"sms_code":"123123",
  	"amount":"20",
  	"payer_id_card":"xxxxxxxxxxx",
  	"payer_bank_card_no":"xxxxxxxxxxxxx",
  	"payer_name":"XXXX",
  	"bank_code":"900000001",
  	"tied_card_type":"1000"
  }
```

**Example（返回报文 例子）**
```
{
    "resp_code": "000012",
    "resp_msg": "订单号重复"
}
```

---
## 2、短信绑卡确认接口

**请求URL：** 
- ` http://localhost:8080/hmdata/api/bankCardVerify/action.json `

**请求参数：**
  参见原文档
  
**请求方式：**
  - POST 


---
## 3、快捷支付接口

**请求URL：** 
- ` http://localhost:8080/hmdata/api/quickPayment/action.json `

**请求参数：**
  参见原文档
  
**请求方式：**
  - POST 
  
---
## 4、支付订单查询接口

**请求URL：** 
- ` http://localhost:8080/hmdata/api/payQueryOrder/action.json `

**请求参数：**
  参见原文档
  
**请求方式：**
  - POST 
  
---
## 5、解除绑定接口

**请求URL：** 
- ` http://localhost:8080/hmdata/api/bankCardRelieve/action.json `

**请求参数：**
  参见原文档
  
**请求方式：**
  - POST 
  
---
## 6、申请提现接口

**请求URL：** 
- ` http://localhost:8080/hmdata/api/drawApply/action.json `

**请求参数：**
参见原文档

**请求方式：**
- POST 