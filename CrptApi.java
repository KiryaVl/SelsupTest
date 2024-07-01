package org.example;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CrptApi {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final Queue<Long> requestTimes;
    private final long intervalMillis;
    private final int requestLimit;
    private final String authToken;


    /**
     * Конструктор класса
     * @param timeUnit - Единица времени для интервала (например, секунды, минуты).
     * @param requestLimit -Максимальное количество запросов, разрешенное в заданном интервале времени.
     */

    public CrptApi(TimeUnit timeUnit, int requestLimit, String authToken) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestTimes = new LinkedList<>();
        this.intervalMillis = timeUnit.toMillis(1);
        this.requestLimit = requestLimit;
        this.authToken = authToken;
    }

    /**
     * Создает документ для ввода в оборот товаров, произведенных в России, и отправляет его в API "Честный знак".
     * @param document - Объект Document, представляющий создаваемый документ.
     * @param signature - Строка, представляющая цифровую подпись.
     * @throws InterruptedException - Выбрасывается, если поток прерывается во время ожидания отправки запроса.
     * @throws IOException - Выбрасывается в случае ошибки ввода-вывода при выполнении HTTP-запроса.
     */
    public synchronized void createDocument(Document document, String signature) throws InterruptedException, IOException {
        ensureRequestLimit();

        String json = objectMapper.writeValueAsString(document);
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://ismp.crpt.ru/api/v3/lk/documents/create")
                .addHeader("Signature", signature)
                .addHeader("Authorization", "Bearer " + authToken) // добавьте ваш токен сюда
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            System.out.println(response.body().string());
        }
    }

    /**
     * Проверяет и управляет количеством запросов, чтобы не превышать заданный лимит.
     * @throws InterruptedException
     */

    private synchronized void ensureRequestLimit() throws InterruptedException {
        long now = System.currentTimeMillis();

        while (requestTimes.size() >= requestLimit) {
            long earliest = requestTimes.peek();
            if (now - earliest < intervalMillis) {
                long waitTime = intervalMillis - (now - earliest);
                Thread.sleep(waitTime);
                now = System.currentTimeMillis();
            } else {
                requestTimes.poll();
            }
        }

        requestTimes.add(now);
    }


    /*
     * Description description - Описание участника.
     * String doc_id - Идентификатор документа.
     * String doc_status - Статус документа.
     * String doc_type - Тип документа, по умолчанию "LP_INTRODUCE_GOODS".
     * boolean importRequest - Флаг запроса импорта.
     * String owner_inn - ИНН владельца.
     * String participant_inn - ИНН участника.
     * String producer_inn - ИНН производителя.
     * String production_date - Дата производства.
     * String production_type - Тип производства.
     * Product[] products - Массив продуктов.
     * String reg_date - Дата регистрации.
     * String reg_number - Регистрационный номер.
     */
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

        /*
         * String participantInn - ИНН участника.
         */
        public static class Description {
            public String participantInn;
        }

        /**
         * String certificate_document - Документ сертификата.
         * String certificate_document_date - Дата документа сертификата.
         * String certificate_document_number - Номер документа сертификата.
         * String owner_inn - ИНН владельца.
         * String producer_inn - ИНН производителя.
         * String production_date - Дата производства.
         * String tnved_code - Код ТН ВЭД.
         * String uit_code - Код УИТ.
         * String uitu_code - Код УИТУ.
         */
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

    public static void main(String[] args) throws IOException, InterruptedException {
        String authToken = "0d90d966-5027-416f-a0cd-0697db8c79f3";

        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5, authToken);

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
}
