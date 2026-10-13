package com.gildedrose;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GildedRoseTest {

    private static final String VEST = "+5 Dexterity Vest";
    private static final String BRIE = "Aged Brie";
    private static final String BACKSTAGE = "Backstage passes to a TAFKAL80ETC concert";
    private static final String SULFURAS = "Sulfuras, Hand of Ragnaros";

    @Mock
    private ItemRepository repository;

    private void updateOneDay(Item... items) {
        when(repository.findAll()).thenReturn(Arrays.asList(items));
        new GildedRose(repository).updateQuality();
    }

    private void assertItem(Item item, int expectedSellIn, int expectedQuality) {
        assertEquals(expectedSellIn, item.sellIn, "sellIn");
        assertEquals(expectedQuality, item.quality, "quality");
    }

    // ---------- 일반 아이템 ----------

    @Test
    @DisplayName("일반 아이템은 유통기한 전에는 품질이 1 감소한다")
    void normalItem_qualityDecreasesByOne() {
        Item item = new Item(VEST, 10, 20);
        updateOneDay(item);
        assertItem(item, 9, 19);
    }

    @Test
    @DisplayName("일반 아이템은 유통기한이 1이어도 품질이 1 감소한다")
    void normalItem_sellInOne_qualityDecreasesByOne() {
        Item item = new Item(VEST, 1, 20);
        updateOneDay(item);
        assertItem(item, 0, 19);
    }

    @Test
    @DisplayName("일반 아이템은 유통기한이 0이면 품질이 2 감소한다")
    void normalItem_sellInZero_qualityDecreasesByTwo() {
        Item item = new Item(VEST, 0, 20);
        updateOneDay(item);
        assertItem(item, -1, 18);
    }

    @Test
    @DisplayName("일반 아이템은 유통기한이 지난 이후에도 품질이 2 감소한다")
    void normalItem_afterExpiry_qualityDecreasesByTwo() {
        Item item = new Item(VEST, -1, 20);
        updateOneDay(item);
        assertItem(item, -2, 18);
    }

    @Test
    @DisplayName("일반 아이템의 품질은 0 아래로 감소하지 않는다")
    void normalItem_qualityNeverNegative() {
        Item item = new Item(VEST, 5, 0);
        updateOneDay(item);
        assertItem(item, 4, 0);
    }

    @Test
    @DisplayName("일반 아이템은 품질이 1일 때 2 감소해야 해도 0이 된다")
    void normalItem_qualityOneAfterExpiry_becomesZero() {
        Item item = new Item(VEST, 0, 1);
        updateOneDay(item);
        assertItem(item, -1, 0);
    }

    @Test
    @DisplayName("일반 아이템은 유통기한이 지난 상태에서 품질이 0이면 0을 유지한다")
    void normalItem_afterExpiryQualityZero_staysZero() {
        Item item = new Item(VEST, -1, 0);
        updateOneDay(item);
        assertItem(item, -2, 0);
    }

    // ---------- Aged Brie ----------

    @Test
    @DisplayName("Aged Brie는 유통기한 전에는 품질이 1 증가한다")
    void agedBrie_qualityIncreasesByOne() {
        Item item = new Item(BRIE, 5, 10);
        updateOneDay(item);
        assertItem(item, 4, 11);
    }

    @Test
    @DisplayName("Aged Brie는 유통기한이 지나면 품질이 2 증가한다")
    void agedBrie_afterExpiry_qualityIncreasesByTwo() {
        Item item = new Item(BRIE, 0, 10);
        updateOneDay(item);
        assertItem(item, -1, 12);
    }

    @Test
    @DisplayName("Aged Brie의 품질은 50을 초과하지 않는다")
    void agedBrie_qualityNeverExceedsFifty() {
        Item item = new Item(BRIE, 5, 50);
        updateOneDay(item);
        assertItem(item, 4, 50);
    }

    @Test
    @DisplayName("Aged Brie는 품질이 49이면 2 증가해야 해도 50까지만 증가한다")
    void agedBrie_qualityCappedAtFiftyAfterExpiry() {
        Item item = new Item(BRIE, 0, 49);
        updateOneDay(item);
        assertItem(item, -1, 50);
    }

    @Test
    @DisplayName("Aged Brie는 유통기한이 지난 상태에서 품질이 50이면 50을 유지한다")
    void agedBrie_afterExpiryQualityFifty_staysFifty() {
        Item item = new Item(BRIE, -1, 50);
        updateOneDay(item);
        assertItem(item, -2, 50);
    }

    // ---------- Backstage passes ----------

    @Test
    @DisplayName("Backstage passes는 공연까지 11일 이상 남으면 품질이 1 증가한다")
    void backstage_elevenDays_qualityIncreasesByOne() {
        Item item = new Item(BACKSTAGE, 11, 20);
        updateOneDay(item);
        assertItem(item, 10, 21);
    }

    @Test
    @DisplayName("Backstage passes는 10일 남으면 품질이 2 증가한다")
    void backstage_tenDays_qualityIncreasesByTwo() {
        Item item = new Item(BACKSTAGE, 10, 20);
        updateOneDay(item);
        assertItem(item, 9, 22);
    }

    @Test
    @DisplayName("Backstage passes는 6일 남아도 품질이 2 증가한다")
    void backstage_sixDays_qualityIncreasesByTwo() {
        Item item = new Item(BACKSTAGE, 6, 20);
        updateOneDay(item);
        assertItem(item, 5, 22);
    }

    @Test
    @DisplayName("Backstage passes는 5일 남으면 품질이 3 증가한다")
    void backstage_fiveDays_qualityIncreasesByThree() {
        Item item = new Item(BACKSTAGE, 5, 20);
        updateOneDay(item);
        assertItem(item, 4, 23);
    }

    @Test
    @DisplayName("Backstage passes는 1일 남아도 품질이 3 증가한다")
    void backstage_oneDay_qualityIncreasesByThree() {
        Item item = new Item(BACKSTAGE, 1, 20);
        updateOneDay(item);
        assertItem(item, 0, 23);
    }

    @Test
    @DisplayName("Backstage passes는 공연이 끝나면 품질이 0이 된다")
    void backstage_afterConcert_qualityDropsToZero() {
        Item item = new Item(BACKSTAGE, 0, 20);
        updateOneDay(item);
        assertItem(item, -1, 0);
    }

    @Test
    @DisplayName("Backstage passes는 10일 남고 품질이 49여도 50을 초과하지 않는다")
    void backstage_tenDays_qualityCappedAtFifty() {
        Item item = new Item(BACKSTAGE, 10, 49);
        updateOneDay(item);
        assertItem(item, 9, 50);
    }

    @Test
    @DisplayName("Backstage passes는 5일 남고 품질이 48이어도 50을 초과하지 않는다")
    void backstage_fiveDays_qualityCappedAtFifty() {
        Item item = new Item(BACKSTAGE, 5, 48);
        updateOneDay(item);
        assertItem(item, 4, 50);
    }

    @Test
    @DisplayName("Backstage passes는 품질이 이미 50이면 증가하지 않는다")
    void backstage_qualityAlreadyFifty_noIncrease() {
        Item item = new Item(BACKSTAGE, 5, 50);
        updateOneDay(item);
        assertItem(item, 4, 50);
    }

    // ---------- Sulfuras ----------

    @Test
    @DisplayName("Sulfuras는 유통기한과 품질이 변하지 않는다")
    void sulfuras_noChange() {
        Item item = new Item(SULFURAS, 10, 80);
        updateOneDay(item);
        assertItem(item, 10, 80);
    }

    @Test
    @DisplayName("Sulfuras는 유통기한이 지난 상태에서도 변하지 않는다")
    void sulfuras_afterExpiry_noChange() {
        Item item = new Item(SULFURAS, -1, 80);
        updateOneDay(item);
        assertItem(item, -1, 80);
    }

    // ---------- 공통 동작 / Repository 상호작용 ----------

    @Test
    @DisplayName("여러 아이템이 있으면 각각의 규칙에 따라 갱신한다")
    void multipleItems_eachUpdatedByOwnRule() {
        Item vest = new Item(VEST, 10, 20);
        Item brie = new Item(BRIE, 5, 10);

        updateOneDay(vest, brie);

        assertItem(vest, 9, 19);
        assertItem(brie, 4, 11);
    }

    @Test
    @DisplayName("갱신된 아이템을 Repository에 저장하도록 요청한다")
    void updateQuality_savesUpdatedItems() {
        Item vest = new Item(VEST, 10, 20);

        updateOneDay(vest);

        verify(repository).saveAll(Arrays.asList(vest));
    }
}
