# Тестовое задание для компании Selsup
Конечно! Я подробно объясню, как работает класс `CrptApi` и что делает каждый метод. Представим, что у вас минимальные знания в программировании.

### Общий обзор

Класс `CrptApi` предназначен для отправки запросов к API "Честный знак". Он обеспечивает потокобезопасность и ограничивает количество запросов в заданный интервал времени. Это значит, что если вы попытаетесь отправить больше запросов, чем разрешено, класс будет ждать, пока можно будет отправить следующий запрос, чтобы не превышать лимит.

### Конструктор

```java
public CrptApi(TimeUnit timeUnit, int requestLimit)
```

- **`TimeUnit timeUnit`** - параметр, который указывает интервал времени (например, секунды, минуты и т.д.).
- **`int requestLimit`** - параметр, который указывает максимальное количество запросов, разрешенных в этом интервале времени.

Этот конструктор создает экземпляр класса `CrptApi` с заданными параметрами. Он инициализирует клиент для отправки запросов и очередь для отслеживания времени отправленных запросов.

### Метод `createDocument`

```java
public synchronized void createDocument(Document document, String signature) throws InterruptedException, IOException
```

- **Параметры**:
  - `document` - объект класса `Document`, представляющий документ, который нужно создать и отправить.
  - `signature` - строка, представляющая цифровую подпись.

- **Исключения**:
  - `InterruptedException` - выбрасывается, если поток прерывается во время ожидания.
  - `IOException` - выбрасывается в случае ошибки ввода-вывода при выполнении HTTP-запроса.

Этот метод:
1. Проверяет, можно ли отправить новый запрос (учитывая лимит запросов).
2. Добавляет текущий момент времени в очередь запросов.
3. Создает JSON-строку из объекта `Document`.
4. Отправляет HTTP POST-запрос к API с JSON-данными и подписью.
5. Обрабатывает ответ от API.

### Вспомогательный метод `ensureRequestLimit`

```java
private synchronized void ensureRequestLimit() throws InterruptedException
```

Этот метод отвечает за проверку, не превышен ли лимит запросов:
1. Получает текущий момент времени.
2. Если количество запросов в очереди превышает лимит, он проверяет, прошло ли достаточно времени с момента первого запроса в очереди.
3. Если прошло, он удаляет старые запросы из очереди.
4. Если не прошло, он рассчитывает время ожидания и приостанавливает выполнение, пока не истечет необходимый интервал времени.

### Внутренние классы

#### Класс `Document`
Этот класс представляет документ для ввода в оборот товаров. Он содержит все необходимые поля для описания документа.

```java
public static class Document {
    public Description description;
    public String doc_id;
    public String doc_status;
    public String doc_type = "LP_INTRODUCE_GOODS";
    public boolean importRequest;
    public String owner_inn;
    public String participant_inn;
    public String producer_inn;
    public String production_date;
    public String production_type;
    public Product[] products;
    public String reg_date;
    public String reg_number;

    public static class Description {
        public String participantInn;
    }

    public static class Product {
        public String certificate_document;
        public String certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }
}
```

### Пример использования

```java
public static void main(String[] args) throws IOException, InterruptedException {
    CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

    Document doc = new Document();
    doc.description = new Document.Description();
    doc.description.participantInn = "123456789";
    doc.doc_id = "doc1";
    doc.doc_status = "DRAFT";
    doc.importRequest = true;
    doc.owner_inn = "123456789";
    doc.participant_inn = "123456789";
    doc.producer_inn = "123456789";
    doc.production_date = "2020-01-23";
    doc.production_type = "type1";
    doc.reg_date = "2020-01-23";
    doc.reg_number = "reg1";
    Document.Product product = new Document.Product();
    product.certificate_document = "doc";
    product.certificate_document_date = "2020-01-23";
    product.certificate_document_number = "123";
    product.owner_inn = "123456789";
    product.producer_inn = "123456789";
    product.production_date = "2020-01-23";
    product.tnved_code = "code";
    product.uit_code = "uit";
    product.uitu_code = "uitu";
    doc.products = new Document.Product[]{product};

    api.createDocument(doc, "signature");
}
```

Этот пример показывает, как создать экземпляр `CrptApi`, создать документ и отправить его в API "Честный знак".

### Итоги

1. **Потокобезопасность**: Класс `CrptApi` использует синхронизацию для обеспечения того, что только один поток может отправлять запросы в любой момент времени.
2. **Ограничение запросов**: Метод `ensureRequestLimit` проверяет и управляет количеством запросов, чтобы не превышать заданный лимит.
3. **Использование HTTP-клиента**: `OkHttpClient` используется для отправки HTTP-запросов.
4. **Сериализация JSON**: `ObjectMapper` используется для преобразования объектов Java в JSON и обратно.
