# 理髮店預約系統規格書 (Barbershop Appointment System Specification)

## 1. 專案概述
本專案為理髮店預約系統，旨在管理預約、設計師排程及顧客關係 (CRM)。系統將與 Line 平台整合，提供使用者互動與通知功能。

## 2. 技術堆疊
- **前端**: Vue 3
- **後端**: Java Spring Boot
- **資料庫**: (待定 - 例如 MySQL/PostgreSQL)
- **整合**: Line Platform (LIFF Web App / Messaging API)

## 3. 功能需求

### 3.1. 服務項目
系統支援以下標準服務及其固定時長：
- **剪+洗**: 1 小時
- **護頭皮**: 1.5 小時
- **染髮**: 2 小時
- **燙髮**: 3 小時

### 3.2. 預約規則
- **營業時間**: 11:00 - 21:00。
- **預約開放時間**: 每月 **15號** 開放預約 *下個月* 的時段。
- **可用性**: 基於設計師的工作時間及現有預約狀況。
- **同時段限制**: 每位設計師同一時間僅能服務一位顧客。
- **取消/修改**: 顧客可隨時取消或修改預約（無時間限制）。
- **付款方式**: 到店付款（無需訂金）。

### 3.3. 指定設計師
- 顧客可以瀏覽設計師列表。
- 顧客可以指定特定設計師進行服務。

### 3.4. 通知 (Line 整合)
系統透過 Line 發送自動通知：
- **預約成功**: 預約後立即發送確認通知。
- **即將到來提醒**: 預約時間前 24 小時發送。

### 3.5. 預約管理 (顧客端)
- 顧客可查詢自己的預約紀錄。
- 顧客可查看即將到來的預約。

### 3.6. 視覺化排班表
- 行事曆/時間軸視圖呈現排班狀況。
- *目標對象*: 主要供員工/管理員管理店內量能。

### 3.7. 設計師班表設定
- **排班設定**: 設定工作日及時間。
- **休假管理**: 設定個別設計師的休假日期。

### 3.8. 顧客管理 (CRM)
- **顧客資料**: 連結 Line 帳號的基本資訊。
- **服務紀錄**: 過去的服務紀錄。
- **髮質檔案**:
    - 消費習慣。
    - 髮質特性。
    - 照片（服務前後對比照或參考圖）。

## 4. 資料模型 (初步)

### 實體 (Entities)
- **User/Customer (使用者/顧客)**: ID, LineID, Name, Phone.
- **Stylist (設計師)**: ID, Name, Specialty.
- **Service (服務)**: ID, Name, Duration, Price.
- **Appointment (預約)**: ID, CustomerID, StylistID, ServiceID, DateTime, Status (Booked, Completed, Cancelled, No-show).
- **Schedule/Availability (排班/可用性)**: StylistID, Date, IsWorking, LeaveType.
- **CRMRecord (CRM紀錄)**: CustomerID, Notes, Photos, Date.
