/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.orderpicking.bootstrap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.acme.orderpicking.domain.Product;

import static org.acme.orderpicking.domain.Shelving.newShelvingId;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_A;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_B;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_C;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_D;
import static org.acme.orderpicking.domain.Warehouse.Column.COL_E;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_1;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_2;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_3;

/**
 * Helper class that emulates the products database.
 *
 * Products are organized into product families, this information can be later used for assigning sets of Shelvigns to
 * the different families, and thus make it more realistic, since normally a family of products are located on the same
 * group of shelvings, etc. Of course real implementations might have much more complex organizations for products.
 *
 * As a general note, the order picking solving problem is agnostic of these families and they are used only for the
 * random data sets generation.
 */
public class ProductDB {

    /**
     * Not part of the model used by the solution. This information is used to distribute the product families into the
     * shelvings. For example, in a supermarket the meat is normally located in a group of neighbor shelvings, the drinks
     * and snacks in another group of neighbor shelvings, etc.
     */
    public enum ProductFamily {
        GENERAL_FOOD,
        FRESH_FOOD,
        MEET_AND_FISH,
        FROZEN_PRODUCTS,
        FRUITS_AND_VEGETABLES,
        HOUSE_CLEANING,
        DRINKS,
        SNACKS,
        PETS
    }

    public static class ProductDBRecord {
        Product product;
        ProductFamily family;

        public ProductDBRecord(Product product, ProductFamily family) {
            this.product = product;
            this.family = family;
        }

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public ProductFamily getFamily() {
            return family;
        }

        public void setFamily(ProductFamily family) {
            this.family = family;
        }

    }

    private static final List<ProductDBRecord> PRODUCTS = new ArrayList<>();

    private static final EnumMap<ProductFamily, List<String>> SHELVINGS_PER_FAMILY = new EnumMap<>(ProductDB.ProductFamily.class);

    private static int id = 0;

    private static String nextId() {
        return Integer.toString(id++);
    }

    static {
        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.FRUITS_AND_VEGETABLES, Arrays.asList(
                newShelvingId(COL_A, ROW_1),
                newShelvingId(COL_A, ROW_2)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.FRESH_FOOD, Collections.singletonList(
                newShelvingId(COL_A, ROW_3)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.MEET_AND_FISH, Arrays.asList(
                newShelvingId(COL_B, ROW_2),
                newShelvingId(COL_B, ROW_3)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.FROZEN_PRODUCTS, Arrays.asList(
                newShelvingId(COL_B, ROW_2),
                newShelvingId(COL_B, ROW_1)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.DRINKS, Collections.singletonList(
                newShelvingId(COL_D, ROW_1)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.SNACKS, Collections.singletonList(
                newShelvingId(COL_D, ROW_2)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.GENERAL_FOOD, Arrays.asList(newShelvingId(COL_B, ROW_2),
                newShelvingId(COL_C, ROW_3),
                newShelvingId(COL_D, ROW_2),
                newShelvingId(COL_D, ROW_3)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.HOUSE_CLEANING, Arrays.asList(newShelvingId(COL_E, ROW_2),
                newShelvingId(COL_E, ROW_1)));

        SHELVINGS_PER_FAMILY.put(ProductDB.ProductFamily.PETS, Collections.singletonList(
                newShelvingId(COL_E, ROW_3)));
    }

    static {
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Kelloggs Cornflakes", 30 * 12 * 35, null), ProductFamily.GENERAL_FOOD));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Cream Crackers", 23 * 7 * 2, null), ProductFamily.GENERAL_FOOD));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Tea Bags 240 packet", 2 * 6 * 15, null), ProductFamily.GENERAL_FOOD));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Tomato Soup Can", 10 * 10 * 10, null), ProductFamily.GENERAL_FOOD));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Baked Beans in Tomato Sauce", 10 * 10 * 10, null), ProductFamily.GENERAL_FOOD));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Classic Mint Sauce", 8 * 10 * 8, null), ProductFamily.GENERAL_FOOD));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Raspberry Conserve", 8 * 10 * 8, null), ProductFamily.GENERAL_FOOD));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Orange Fine Shred Marmalade", 7 * 8 * 7, null), ProductFamily.GENERAL_FOOD));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Free Range Eggs 6 Pack", 15 * 10 * 8, null), ProductFamily.FRESH_FOOD));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Mature Cheddar 400G", 10 * 9 * 5, null), ProductFamily.FRESH_FOOD));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Butter Packet", 12 * 5 * 5, null), ProductFamily.FRESH_FOOD));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Iceberg Lettuce Each", 2500, null), ProductFamily.FRUITS_AND_VEGETABLES));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Carrots 1Kg", 1000, null), ProductFamily.FRUITS_AND_VEGETABLES));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Organic Fair Trade Bananas 5 Pack", 1800, null), ProductFamily.FRUITS_AND_VEGETABLES));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Gala Apple Minimum 5 Pack", 25 * 20 * 10, null), ProductFamily.FRUITS_AND_VEGETABLES));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Orange Bag 3kg", 29 * 20 * 15, null), ProductFamily.FRUITS_AND_VEGETABLES));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Fairy Non Biological Laundry Liquid 4.55L", 5000, null), ProductFamily.HOUSE_CLEANING));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Toilet Tissue 8 Roll White", 50 * 20 * 20, null), ProductFamily.HOUSE_CLEANING));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Kitchen Roll 200 Sheets x 2", 30 * 30 * 15, null), ProductFamily.HOUSE_CLEANING));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Stainless Steel Cleaner 500Ml", 500, null), ProductFamily.HOUSE_CLEANING));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Antibacterial Surface Spray", 12 * 4 * 25, null), ProductFamily.HOUSE_CLEANING));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Beef Lean Steak Mince 500g", 500, null), ProductFamily.MEET_AND_FISH));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Smoked Salmon 120G", 150, null), ProductFamily.MEET_AND_FISH));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Steak Burgers 454G", 450, null), ProductFamily.MEET_AND_FISH));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Pork Cooked Ham 125G", 125, null), ProductFamily.MEET_AND_FISH));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Chicken Breast Fillets 300G", 300, null), ProductFamily.MEET_AND_FISH));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "6 Milk Bricks Pack", 22 * 16 * 21, null), ProductFamily.DRINKS));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Milk Brick", 1232, null), ProductFamily.DRINKS));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Skimmed Milk 2.5L", 2500, null), ProductFamily.DRINKS));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "3L Orange Juice", 3 * 1000, null), ProductFamily.DRINKS));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Alcohol Free Beer 4 Pack", 30 * 15 * 30, null), ProductFamily.DRINKS));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Pepsi Regular Bottle", 1000, null), ProductFamily.DRINKS));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Pepsi Diet 6 x 330ml", 35 * 12 * 12, null), ProductFamily.DRINKS));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Schweppes Lemonade 2L", 2000, null), ProductFamily.DRINKS));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Coke Zero 8 x 330ml", 40 * 12 * 12, null), ProductFamily.DRINKS));
        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Natural Mineral Water Still 6 X 1.5Ltr", 6 * 1500, null), ProductFamily.DRINKS));

        PRODUCTS.add(new ProductDBRecord(new Product(nextId(), "Cocktail Crisps 6 Pack", 20 * 10 * 10, null), ProductFamily.SNACKS));
    }

    /**
     * As a problem simplification, it can be assumed that products will never have a greater volume that the selected
     * trolleys bucket capacity. This method is used establishing a lower boundary on those values.
     */
    public static int getMaxProductSize() {
        return getProducts().stream()
                .mapToInt(dbRecord -> dbRecord.getProduct().getVolume())
                .max().orElse(0);
    }

    public static List<ProductDBRecord> getProducts() {
        return PRODUCTS;
    }

    public static List<String> getShelvings(ProductDB.ProductFamily productFamily) {
        return SHELVINGS_PER_FAMILY.get(productFamily);
    }
}
