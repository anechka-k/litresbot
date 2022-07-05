package litresbot.opdssearch.opds;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.Assert;
import static org.mockito.Mockito.*;

import litresbot.http.HttpClientWithProxy;

public class CrawlerTest {
  String searchResultPage1 =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n <feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:dc=\"http://purl.org/dc/terms/\" xmlns:os=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:opds=\"http://op" +
    "ds-spec.org/2010/catalog\"> <id>tag:search:books:книга:</id>\n <title>Результат поиска</title>\n <updated>2022-07-06T00:43:41+02:00</updated>\n <icon>/favicon.ico</icon>\n <link href=\"/opds-opensearc" +
    "h.xml\" rel=\"search\" type=\"application/opensearchdescription+xml\" />\n <link href=\"/opds/search?searchTerm={searchTerms}\" rel=\"search\" type=\"application/atom+xml\" />\n <link href=\"/opds\" r" +
    "el=\"start\" type=\"application/atom+xml;profile=opds-catalog\" />\n <link href=\"/opds/search?searchTerm=%D0%BA%D0%BD%D0%B8%D0%B3%D0%B0\" rel=\"up\" type=\"application/atom+xml;profile=opds-catalog\"" +
    " />\n <link href=\"/opds/search?searchType=books&amp;searchTerm=%D0%BA%D0%BD%D0%B8%D0%B3%D0%B0&amp;pageNumber=1\" rel=\"next\" type=\"application/atom+xml;profile=opds-catalog\" />\n <entry> <updated>" +
    "2022-07-06T00:43:41+02:00</updated>\n <title>   Книга о слонах</title>\n <author> <name>Бауэр Ганс</name>\n <uri>/a/127691</uri>\n</author>\n <link href=\"/opds/author/127691\" rel=\"related\" type=\"" +
    "application/atom+xml\" title=\"Все книги автора Бауэр Ганс\" />\n <category term=\"Зоология\" label=\"Зоология\" />\n <dc:language>ru</dc:language>\n <dc:format>pdf</dc:format>\n <dc:issued>1964</dc:i" +
    "ssued>\n <content type=\"text/html\">Эта книга об одних из наиболее интересных и популярных животных земного шара. Ее автор — немецкий писатель Ганс Бауэр  стремился сообщить о них самые разнообразные" +
    " сведения. Живо и увлекательно рассказывает он о жизни слонов среди природы и в неволе в давно минувшие и нынешние времена, об удивительных физиологических особенностях этих гигантских представителей " +
    "животного мира, о том, как ловят слонов и как их приручают и дрессируют, о ценнейших слоновых бивнях — слоновой кости и о многих других интереснейших особенностях этих своеобразных животных.\n\n«Мысль" +
    "»,1964. — 175 с.&lt;br/&gt;Год издания: 1964&lt;br/&gt;Формат: pdf&lt;br/&gt;Язык: ru&lt;br/&gt;Размер: 9020 Kb&lt;br/&gt;Скачиваний: 497&lt;br/&gt;</content>\n <link href=\"/b/350913/download\" rel=\"" +
    "http://opds-spec.org/acquisition/open-access\" type=\"application/pdf\" />\n <link href=\"/b/350913\" rel=\"alternate\" type=\"text/html\" title=\"Книга на сайте\" />\n <id>tag:book:ae76ead09d3b2e9d4" +
    "d96ffb555a26898</id>\n</entry>\n <entry> <updated>2022-07-06T00:43:41+02:00</updated>\n <title> Путь в беспредельность (книга 4)</title>\n <author> <name>Быстров Николай Семенович</name>\n <uri>/a/161" +
    "999</uri>\n</author>\n <link href=\"/opds/author/161999\" rel=\"related\" type=\"application/atom+xml\" title=\"Все книги автора Быстров Николай Семенович\" />\n <category term=\"Неотсортированное\" l" +
    "abel=\"Неотсортированное\" />\n <dc:language>ru</dc:language>\n <dc:format>pdf</dc:format>\n <dc:issued>2015</dc:issued>\n <content type=\"text/html\">Год издания: 2015&lt;br/&gt;Формат: pdf&lt;br/&gt" +
    ";Язык: ru&lt;br/&gt;Размер: 825 Kb&lt;br/&gt;Скачиваний: 156&lt;br/&gt;</content>\n <link href=\"/b/435470/download\" rel=\"http://opds-spec.org/acquisition/open-access\" type=\"application/pdf\" />\n" +
    " <link href=\"/b/435470\" rel=\"alternate\" type=\"text/html\" title=\"Книга на сайте\" />\n <id>tag:book:1d059f06f8c57537d40ec8816a0549f3</id>\n</entry>\n</feed>\n";

  String searchResultPage2 =
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n <feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:dc=\"http://purl.org/dc/terms/\" xmlns:os=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:opds=\"http://op" +
    "ds-spec.org/2010/catalog\"> <id>tag:search:books:тиньков:</id>\n <title>Результат поиска</title>\n <updated>2022-07-03T01:14:36+02:00</updated>\n <icon>/favicon.ico</icon>\n <link href=\"/opds-opensea" +
    "rch.xml\" rel=\"search\" type=\"application/opensearchdescription+xml\" />\n <link href=\"/opds/search?searchTerm={searchTerms}\" rel=\"search\" type=\"application/atom+xml\" />\n <link href=\"/opds\"" +
    " rel=\"start\" type=\"application/atom+xml;profile=opds-catalog\" />\n <link href=\"/opds/search?searchTerm=%D1%82%D0%B8%D0%BD%D1%8C%D0%BA%D0%BE%D0%B2\" rel=\"up\" type=\"application/atom+xml;profile=" +
    "opds-catalog\" />\n <entry> <updated>2022-07-03T01:14:36+02:00</updated>\n <title>Не такой как Тиньков</title>\n <author> <name>Шейтельман Михаил</name>\n <uri>/a/155960</uri>\n</author>\n <link href=" +
    "\"/opds/author/155960\" rel=\"related\" type=\"application/atom+xml\" title=\"Все книги автора Шейтельман Михаил\" />\n <category term=\"Карьера, кадры\" label=\"Карьера, кадры\" />\n <category term=\"" +
    "Самосовершенствование\" label=\"Самосовершенствование\" />\n <dc:language>ru</dc:language>\n <dc:format>fb2+zip</dc:format>\n <dc:issued>2015</dc:issued>\n <content type=\"text/html\">\n    &lt;p cla" +
    "ss=&quot;book&quot;&gt;Миллион книг рассказывают, как заработать миллион. Рисковать всем и любить работать хотя бы по 16 часов в день. Я не готов рисковать всем и не очень люблю работать. Если вы по т" +
    "ой же причине до сих пор не заработали миллион, попробую научить вас зарабатывать для начала 100–200 тысяч долларов в год. Конечно же, не ходя каждый день на работу. Ведь вы, как и я, не очень любите " +
    "работать.&lt;/p&gt;\n   &lt;br/&gt;Год издания: 2015&lt;br/&gt;Формат: fb2&lt;br/&gt;Язык: ru&lt;br/&gt;Размер: 464 Kb&lt;br/&gt;Скачиваний: 1278&lt;br/&gt;</content>\n <link href=\"/i/22/423022/cover" +
    ".jpg\" rel=\"http://opds-spec.org/image\" type=\"image/jpeg\" />\n <link href=\"/i/22/423022/cover.jpg\" rel=\"x-stanza-cover-image\" type=\"image/jpeg\" />\n <link href=\"/i/22/423022/cover.jpg\" rel" +
    "=\"http://opds-spec.org/thumbnail\" type=\"image/jpeg\" />\n <link href=\"/i/22/423022/cover.jpg\" rel=\"x-stanza-cover-image-thumbnail\" type=\"image/jpeg\" />\n <link href=\"/b/423022/fb2\" rel=\"ht" +
    "tp://opds-spec.org/acquisition/open-access\" type=\"application/fb2+zip\" />\n <link href=\"/b/423022/html\" rel=\"http://opds-spec.org/acquisition/open-access\" type=\"application/html+zip\" />\n <li" +
    "nk href=\"/b/423022/txt\" rel=\"http://opds-spec.org/acquisition/open-access\" type=\"application/txt+zip\" />\n <link href=\"/b/423022/rtf\" rel=\"http://opds-spec.org/acquisition/open-access\" type=" +
    "\"application/rtf+zip\" />\n <link href=\"/b/423022/epub\" rel=\"http://opds-spec.org/acquisition/open-access\" type=\"application/epub+zip\" />\n <link href=\"/b/423022/mobi\" rel=\"http://opds-spec." +
    "org/acquisition/open-access\" type=\"application/x-mobipocket-ebook\" />\n <link href=\"/b/423022\" rel=\"alternate\" type=\"text/html\" title=\"Книга на сайте\" />\n <id>tag:book:1d8ed3c7fc2c8a01784b" +
    "806942900463</id>\n</entry>\n <entry> <updated>2022-07-03T01:14:36+02:00</updated>\n <title>СуперФирма: Краткий курс по раскрутке. От Тинькова до Чичваркина</title>\n <author> <name>Масленников Роман " +
    "Михайлович</name>\n <uri>/a/61957</uri>\n</author>\n <link href=\"/opds/author/61957\" rel=\"related\" type=\"application/atom+xml\" title=\"Все книги автора Масленников Роман Михайлович\" />\n <categ" +
    "ory term=\"Маркетинг, PR\" label=\"Маркетинг, PR\" />\n <dc:language>ru</dc:language>\n <dc:format>fb2+zip</dc:format>\n <content type=\"text/html\">&lt;p class=book&gt;Нет такого предпринимателя, мар" +
    "кетолога, создателя продукта или услуги, который не мучился бы вопросами:&lt;/p&gt;&lt;p class=book&gt;• как назвать свою компанию, продукт, акцию?&lt;/p&gt;&lt;p class=book&gt;• как привлечь новых кл" +
    "иентов и сделать так, чтобы они стали постоянными?&lt;/p&gt;&lt;p class=book&gt;• как создать свою фишку, которая выделит фирму из толпы конкурентов?&lt;/p&gt;&lt;p class=book&gt;• что важно помнить, " +
    "создавая новую компанию (начиная проект), чтобы потом &quot;нераскрученность&quot; фирмы или марки не тормозила развитие бизнеса?&lt;/p&gt;&lt;p class=book&gt;Книга обобщает практический опыт более 10" +
    "0 компаний и состоит из 49 шагов, последовательное применение которых обязательно приведет вашу фирму к отличным результатам. Такие успешные бизнесмены, как Павел Теплухин, Сергей Полонский, Олег Тинь" +
    "ков, Евгений Чичваркин, и многие другие делятся на ее страницах собственным опытом раскрутки.&lt;/p&gt;&lt;p class=book&gt;Книга предназначена руководителям фирм, как уже действующих, так и проектируе" +
    "мых, менеджерам по маркетингу, рекламе и PR.&lt;/p&gt;&lt;br/&gt;Формат: fb2&lt;br/&gt;Язык: ru&lt;br/&gt;Размер: 963 Kb&lt;br/&gt;Скачиваний: 2161&lt;br/&gt;</content>\n <link href=\"/i/53/331653/cov" +
    "er.jpg\" rel=\"http://opds-spec.org/image\" type=\"image/jpeg\" />\n <link href=\"/i/53/331653/cover.jpg\" rel=\"x-stanza-cover-image\" type=\"image/jpeg\" />\n <link href=\"/i/53/331653/cover.jpg\" r" +
    "el=\"http://opds-spec.org/thumbnail\" type=\"image/jpeg\" />\n <link href=\"/i/53/331653/cover.jpg\" rel=\"x-stanza-cover-image-thumbnail\" type=\"image/jpeg\" />\n <link href=\"/b/331653/fb2\" rel=\"" +
    "http://opds-spec.org/acquisition/open-access\" type=\"application/fb2+zip\" />\n <link href=\"/b/331653/html\" rel=\"http://opds-spec.org/acquisition/open-access\" type=\"application/html+zip\" />\n <" +
    "link href=\"/b/331653/txt\" rel=\"http://opds-spec.org/acquisition/open-access\" type=\"application/txt+zip\" />\n <link href=\"/b/331653/rtf\" rel=\"http://opds-spec.org/acquisition/open-access\" typ" +
    "e=\"application/rtf+zip\" />\n <link href=\"/b/331653/epub\" rel=\"http://opds-spec.org/acquisition/open-access\" type=\"application/epub+zip\" />\n <link href=\"/b/331653/mobi\" rel=\"http://opds-spe" +
    "c.org/acquisition/open-access\" type=\"application/x-mobipocket-ebook\" />\n <link href=\"/b/331653\" rel=\"alternate\" type=\"text/html\" title=\"Книга на сайте\" />\n <id>tag:book:a5b3b4e1e27f374ed5" +
    "0aa93016d6ed33</id>\n</entry>\n</feed>\n";

  @Test 
  public void testDownloadPageOk() throws IOException {
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenReturn(searchResultPage2);
    Crawler crawler = new Crawler(mockHttp);
    Page page = crawler.downloadPage("url");
    Assert.assertNull(page.title); 
    Assert.assertEquals(2, page.entries.size());
    Assert.assertEquals(4, page.links.size());
  }

  @Test 
  public void testDownloadPageNotFound() throws IOException {
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenThrow(new IOException("Got status: 404"));
    Crawler crawler = new Crawler(mockHttp);
    Assert.assertThrows(IOException.class, () -> { crawler.downloadPage("url"); });
  }

  @Test 
  public void testDownloadPageEmpty() throws IOException {
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenReturn("");
    Crawler crawler = new Crawler(mockHttp);
    Page page = crawler.downloadPage("url");
    Assert.assertNull(page.title);
    Assert.assertEquals(0, page.entries.size());
    Assert.assertEquals(0, page.links.size());
  }

  @Test 
  public void testDownloadPageBad() throws IOException {
    String badResult = "<?bad";
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenReturn(badResult);
    Crawler crawler = new Crawler(mockHttp);
    Page page = crawler.downloadPage("url");
    Assert.assertNull(page.title);
    Assert.assertEquals(0, page.entries.size());
    Assert.assertEquals(0, page.links.size());
  }

  @Test 
  public void testDownloadCatalogOk() throws IOException {
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenReturn(searchResultPage1).thenReturn(searchResultPage2);
    Crawler crawler = new Crawler(mockHttp);
    List<Page> pages = crawler.downloadCatalog("root", "url");
    Assert.assertEquals(2, pages.size());
    Assert.assertNull(pages.get(0).title);
    Assert.assertEquals(2, pages.get(0).entries.size());
    Assert.assertEquals(5, pages.get(0).links.size());
    Assert.assertNull(pages.get(1).title);
    Assert.assertEquals(2, pages.get(1).entries.size());
    Assert.assertEquals(4, pages.get(1).links.size());
  }

  @Test 
  public void testDownloadCatalogCyclicFirst() throws IOException {
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenReturn(searchResultPage1).thenReturn(searchResultPage1).thenReturn(searchResultPage1);
    Crawler crawler = new Crawler(mockHttp);
    List<Page> pages = crawler.downloadCatalog("root", "/opds/search?searchType=books&searchTerm=%D0%BA%D0%BD%D0%B8%D0%B3%D0%B0&pageNumber=1");
    Assert.assertEquals(1, pages.size());
    Assert.assertNull(pages.get(0).title);
    Assert.assertEquals(2, pages.get(0).entries.size());
    Assert.assertEquals(5, pages.get(0).links.size());
  }

  @Test 
  public void testDownloadCatalogCyclicNext() throws IOException {
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenReturn(searchResultPage1).thenReturn(searchResultPage1).thenReturn(searchResultPage1);
    Crawler crawler = new Crawler(mockHttp);
    List<Page> pages = crawler.downloadCatalog("root", "url");
    Assert.assertEquals(2, pages.size());
    Assert.assertNull(pages.get(0).title);
    Assert.assertEquals(2, pages.get(0).entries.size());
    Assert.assertEquals(5, pages.get(0).links.size());
    Assert.assertNull(pages.get(1).title);
    Assert.assertEquals(2, pages.get(1).entries.size());
    Assert.assertEquals(5, pages.get(1).links.size());
  }

  @Test 
  public void testDownloadCatalogBad() throws IOException {
    String badResult = "<?bad";
    HttpClientWithProxy mockHttp = mock(HttpClientWithProxy.class);
    when(mockHttp.sendGetRequest(anyString())).thenReturn(badResult);
    Crawler crawler = new Crawler(mockHttp);
    List<Page> pages = crawler.downloadCatalog("root", "url");
    Assert.assertEquals(1, pages.size());
    Assert.assertNull(pages.get(0).title);
    Assert.assertEquals(0, pages.get(0).entries.size());
    Assert.assertEquals(0, pages.get(0).links.size());
  }
}
