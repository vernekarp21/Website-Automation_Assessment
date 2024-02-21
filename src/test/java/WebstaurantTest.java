import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class WebstaurantTest {

    private WebDriver driver;
    private String baseUrl;


    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        baseUrl = "https://www.webstaurantstore.com/";
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    @Test
    public void verifyTest() throws InterruptedException {

        // Step1: Go to WebstaurantStore
        driver.get(baseUrl);

        // Step2:  Search for 'stainless work table'
        WebElement searchBox = driver.findElement(By.id("searchval"));
        searchBox.sendKeys("stainless work table");

        // Step3:  Check the search result ensuring every product has the word 'Table' in its title.
        List<WebElement> products = driver.findElements(By.xpath("//ul[@id='awesomplete_list_1']/li//following-sibling::li/span[2]"));
        boolean searchTitleContainsTable = true;
        for (WebElement product : products) {
            String titles = product.getText();
            if (!titles.toLowerCase().contains("table")) {
                searchTitleContainsTable = false;
                break;
            }
        }
        Assert.assertTrue(searchTitleContainsTable, "Not all product titles contain the word Table.");
        searchBox.submit();

        // Step4 : Add the last of found items to Cart - Navigate to last page and select the last found item.
        driver.findElement(By.xpath("//div[@id='paging']/nav/ul/li[last()-1]/a")).click();
        driver.findElement(By.xpath("//div[@id='product_listing']/div[last()]")).click();
        driver.findElement(By.id("buyButton")).click();

        // Waiting for cart to update with the new item added to the cart
        WebElement cartItem = driver.findElement(By.id("cartItemCountSpan"));
        int cartCount = Integer.parseInt(cartItem.getText());
        int currentCount = cartCount;
        while (cartCount == currentCount) {
            // Checking the cart count every 0.1 second
            Thread.sleep(100);
            currentCount = Integer.parseInt(cartItem.getText());
        }

        driver.findElement(By.xpath("//a[@data-testid='cart-button']")).click();

        // waiting for Cart page to load
        WebDriverWait wait = new WebDriverWait(driver, 3);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='cartItems']")));

        // Step5 : Empty Cart.
        driver.findElement(By.xpath("//button[text() = \"Empty Cart\"]")).click();
        driver.findElement(By.xpath("//button[@type='button' and text() = \"Empty\"]")).click();

        // Checking if the Cart is Empty
        String emptyCartText = driver.findElement(By.xpath("//div[@class='empty-cart__text']/p[1]")).getText();
        cartItem = driver.findElement(By.id("cartItemCountSpan"));
        cartCount = Integer.parseInt(cartItem.getText());
        Assert.assertEquals(cartCount, 0, "The Cart count is not set to 0");
        Assert.assertEquals(emptyCartText, "Your cart is empty.", "The Empty Cart message was not displayed.");

    }

    @AfterMethod
    public void tearDown() {
        // Close the browser
        driver.close();
    }
}
