package com.example.data.datasource

import com.example.data.model.DietaryCategory
import com.example.data.model.MealItem

object PlannerData {

    val categories: List<DietaryCategory> = listOf(
        DietaryCategory(
            id = "CAT-01",
            name = "Heavy (Whole-Food High Protein)",
            macro = "40% Protein | 25% Net Carbs | 35% Fats (Min 2.0g/kg Protein)",
            philosophy = "Dense, unprocessed animal & plant whole proteins; zero refined sugar, zero protein powders, zero processed bars, zero artificial sweeteners. Pure real-food power.",
            rule = "If user is on ACE-inhibitors/ARBs or renal impairment, protein is capped at 1.4g/kg and potassium-heavy foods are monitored.",
            rotation = "Main protein source must rotate every day (e.g. Day 1: Beef/Eggs -> Day 2: Salmon/Chicken -> Day 3: Bison/Turkey -> Day 4: Fish/Lentils). No repeating primary protein consecutively.",
            token = "Heavy"
        ),
        DietaryCategory(
            id = "CAT-02",
            name = "Longevity & Cellular Autophagy",
            macro = "18% Protein | 45% Complex Carbs | 37% Fats (mTOR cycling)",
            philosophy = "Periodic low-protein fasting-mimicking framework; sirtuin-activating polyphenols, spermidine-rich botanicals, caloric density moderation.",
            rule = "If on Metformin or SGLT2i, ensure adequate complex carbs to avoid hypoglycemia; avoids grapefruit if on statins.",
            rotation = "Carbohydrate sources rotate between tubers, ancient grains, and legumes across consecutive days.",
            token = "Longevity"
        ),
        DietaryCategory(
            id = "CAT-03",
            name = "Neuro-Fuel & Cognitive Peak",
            macro = "25% Protein | 30% Low-GI Carbs | 45% Brain Fats",
            philosophy = "DHA-rich marine lipids, acetylcholine precursors (choline), lutein, flavanols, and steady low-glycemic fuel to eliminate brain fog and mental crashes.",
            rule = "If user is taking MAOIs or SSRIs, tyramine-heavy aged foods and tryptophan imbalances are screened and moderated.",
            rotation = "Omega-3 fish and poultry alternate on consecutive days; brain-berry antioxidant profiles alternate between dark berries and citrus flavonoids.",
            token = "Neuro-Fuel"
        ),
        DietaryCategory(
            id = "CAT-04",
            name = "Metabolic Reset (Clean Keto)",
            macro = "25% Protein | 5% Net Carbs (<30g/day) | 70% Healthy Fats",
            philosophy = "Therapeutic carbohydrate restriction to induce physiological ketosis, restore insulin sensitivity, and utilize ketone bodies for cellular energy.",
            rule = "If on SGLT2i inhibitors or insulin, ketosis requires strict clinical hydration and physician clearance to avoid euglycemic DKA.",
            rotation = "Cruciferous vegetables and leafy green choices rotate daily; healthy cooking fat sources alternate between olive oil, avocado oil, and grass-fed ghee.",
            token = "Metabolic Reset"
        ),
        DietaryCategory(
            id = "CAT-05",
            name = "Microbiome Shield & Anti-Inflammatory",
            macro = "22% Protein | 48% Prebiotic Carbs | 30% Polyphenol Fats",
            philosophy = "30+ diverse plant species per week; prebiotic soluble fibers, resistant starches, and live fermented probiotic cultures to rebuild gut epithelial lining.",
            rule = "If taking immunosuppressants or active IBD flare, raw unpasteurized ferments are substituted with cooked prebiotic fiber.",
            rotation = "No identical legume or grain source repeated two days in a row; ferment source alternates daily (e.g. Kimchi -> Sauerkraut -> Miso -> Kefir).",
            token = "Microbiome Shield"
        )
    )

    val meals: List<MealItem> = listOf(
        // HEAVY
        MealItem("HVY-001", "Heavy", "Breakfast", "Grass-Fed Steak & Pastured Eggs Power Plate", "180g Grass-fed tenderloin steak, 3 pastured whole eggs, 1/2 sliced avocado, 1 cup baby spinach sautéed in extra virgin olive oil", 640, 54, 3, 44, 5, "Heme Iron, Choline, Vitamin B12, Creatine, Lutein", 15),
        MealItem("HVY-002", "Heavy", "Breakfast", "Greek Yogurt, Nut Butter & Raw Seed Superbowl", "250g Plain 0% Greek Yogurt, 2 tbsp pure almond butter (100% almonds), 1 tbsp chia seeds, 1 tbsp hemp hearts, 1/2 cup organic blueberries", 480, 42, 16, 22, 8, "Complete dairy protein, Calcium, Alpha-Linolenic Acid (Omega-3), Anthocyanins", 5),
        MealItem("HVY-003", "Heavy", "Breakfast", "Smoked Salmon & Pastured 4-Egg Scramble with Chives", "120g Wild Alaskan smoked salmon, 4 pastured eggs, 1 tbsp ghee, fresh chopped chives, 1 cup grilled cherry tomatoes", 520, 46, 4, 36, 2, "EPA/DHA Marine Omega-3s, Choline, Vitamin D, Astaxanthin", 12),
        MealItem("HVY-004", "Heavy", "Breakfast", "Bison Breakfast Hash with Sweet Potato & Herbs", "180g Ground grass-fed bison (90/10), 100g diced roasted sweet potato, 1 cup kale, 1 pastured fried egg, rosemary", 550, 48, 24, 26, 5, "Zinc, Bioavailable Iron, Vitamin A (Beta-Carotene), CLA", 20),
        MealItem("HVY-006", "Heavy", "Lunch", "Pan-Seared Wild Salmon with Roasted Asparagus & Quinoa", "200g Wild Sockeye salmon fillet, 150g roasted asparagus spears, 1/2 cup cooked organic quinoa, lemon-olive oil drizzle", 610, 52, 22, 32, 6, "Astaxanthin, Potassium, Folate, Marine Omega-3 Fatty Acids", 20),
        MealItem("HVY-007", "Heavy", "Lunch", "Herb-Roasted Chicken Breast with Sweet Potato & Broccolini", "220g Free-range chicken breast, 150g baked sweet potato cubes, 1 cup steamed broccolini, 1 tbsp extra virgin olive oil", 580, 56, 30, 21, 7, "Niacin (B3), Sulforaphane, Potassium, Beta-Carotene", 25),
        MealItem("HVY-008", "Heavy", "Lunch", "Bison Burger Bowl with Avocado, Eggs & Mixed Greens", "200g Grass-fed bison patty, 1 fried egg, 1/2 sliced avocado, 2 cups mixed spring greens, olive oil & lemon vinaigrette", 650, 55, 6, 44, 6, "High Bioavailable Heme Iron, Zinc, CoQ10, Choline", 18),
        MealItem("HVY-011", "Heavy", "Dinner", "Grass-Fed Ribeye Steak with Garlic Herb Mushrooms & Asparagus", "250g Grass-fed ribeye steak, 1 cup button & shiitake mushrooms pan-seared in grass-fed butter, 150g grilled asparagus", 720, 58, 5, 52, 5, "Creatine, Carnosine, Ergothioneine, Vitamin B12, Heme Iron", 20),
        MealItem("HVY-012", "Heavy", "Dinner", "Crispy Skin Barramundi with Roasted Root Medley & Chimichurri", "220g Wild-caught Barramundi fillet, 100g roasted carrots & parsnips, fresh parsley-oregano chimichurri dressing", 590, 50, 18, 32, 5, "High EPA/DHA, Vitamin A, Carvacrol, Rosmarinic acid", 25),
        MealItem("HVY-016", "Heavy", "Snack", "Pastured Hard-Boiled Eggs with Guacamole & Hemp Seeds", "3 Pastured hard-boiled eggs, 3 tbsp fresh guacamole, 1 tbsp raw shelled hemp hearts, pinch of Celtic sea salt", 360, 24, 4, 26, 4, "Choline, Lutein, Zeaxanthin, Essential Fatty Acids", 5),
        MealItem("HVY-017", "Heavy", "Snack", "Grass-Fed Beef Biltong & Raw Sprouted Almonds", "60g Organic artisanal grass-fed beef biltong (0g sugar), 30g raw sprouted almonds", 340, 38, 3, 18, 4, "Complete animal amino acids, Vitamin E, Magnesium, Iron", 2),
        // LONGEVITY
        MealItem("LNG-001", "Longevity", "Breakfast", "Sirtuin-Activating Matcha Walnut Chia Pudding", "3 tbsp Chia seeds soaked in unsweetened almond milk, 1 tsp ceremonial matcha, 25g raw walnuts, 1/3 cup wild blueberries", 380, 12, 16, 26, 14, "EGCG, Polyphenols, Alpha-Linolenic Acid, Sirtuin activators", 10),
        MealItem("LNG-002", "Longevity", "Lunch", "Mediterranean Sardine & Warm Puy Lentil Salad", "1 Can wild sardines, 1 cup cooked French green lentils, diced red onion, parsley, capers, 1.5 tbsp extra virgin olive oil", 510, 36, 32, 24, 11, "Spermidine, Hydroxytyrosol, Resveratrol, Marine Omega-3s", 15),
        MealItem("LNG-003", "Longevity", "Dinner", "Tempeh & Shiitake Mushroom Stir-Fry with Bok Choy", "180g Organic fermented tempeh, 1 cup fresh shiitake mushrooms, 2 cups bok choy, fresh ginger-garlic tamari glaze", 460, 34, 22, 24, 10, "Beta-Glucans, Isoflavones, Ergothioneine, Glucosinolates", 20),
        MealItem("LNG-004", "Longevity", "Lunch", "Wild Sockeye Salmon with Turmeric Roasted Cauliflower", "180g Wild salmon, 2 cups cauliflower florets tossed in turmeric and black pepper, 1 cup steamed kale, olive oil", 540, 44, 12, 32, 6, "Curcumin bio-absorption, Astaxanthin, Sulforaphane", 25),
        MealItem("LNG-005", "Longevity", "Dinner", "Artichoke, Olive & White Bean Tuscan Stew", "1 cup Cooked cannellini beans, 1 cup artichoke hearts, 10 kalamata olives, rosemary, garlic, rich tomato broth", 420, 18, 46, 14, 15, "Inulin prebiotic fiber, Cynarin, Oleuropein, Lycopene", 25),
        MealItem("LNG-006", "Longevity", "Snack", "Sprouted Pumpkin Seeds & Wild Blackberries", "35g Raw sprouted pumpkin seeds, 1/2 cup fresh wild blackberries", 220, 12, 12, 14, 6, "Zinc, Magnesium, Ellagic Acid", 2),
        // NEURO-FUEL
        MealItem("NRO-001", "Neuro-Fuel", "Breakfast", "DHA Brain Bowl with Pastured Eggs, Avocado & Mackerel", "2 Pastured poached eggs, 100g wild mackerel fillet, 1/2 sliced avocado, 1 cup baby spinach with cold-pressed olive oil", 520, 38, 4, 38, 5, "Choline, Marine DHA/EPA, Vitamin D3, Lutein", 15),
        MealItem("NRO-002", "Neuro-Fuel", "Lunch", "Rosemary-Crusted Wild Salmon with Blueberry Walnut Salad", "180g Wild salmon, 2 cups baby arugula, 1/2 cup fresh blueberries, 30g raw walnuts, extra virgin olive oil dressing", 580, 44, 14, 36, 6, "Rosmarinic acid, Anthocyanins, DHA, Neuro-protective polyphenols", 18),
        MealItem("NRO-003", "Neuro-Fuel", "Dinner", "Grass-Fed Beef Liver & Caramelized Onion Skillet with Greens", "150g Organic grass-fed calf liver, 1 sliced yellow onion, 2 cups sautéed lacinato kale in ghee, steamed sweet potato", 490, 42, 26, 18, 5, "Concentrated Vitamin A, Vitamin B12, Bioavailable Choline, Copper", 20),
        MealItem("NRO-004", "Neuro-Fuel", "Lunch", "Free-Range Turkey & Avocado Lettuce Wraps with Raw Cacao", "180g Sliced roast turkey breast, 1 whole avocado in crisp romaine boats, side of 20g 100% dark raw cacao nibs", 460, 44, 8, 26, 9, "Tryptophan, Flavanols, Monounsaturated fats, Potassium", 10),
        MealItem("NRO-005", "Neuro-Fuel", "Dinner", "Pan-Seared Rainbow Trout with Watercress & Pumpkin Seeds", "200g Fresh rainbow trout, 2 cups fresh watercress, 2 tbsp toasted raw pumpkin seeds, lemon-garlic dressing", 510, 46, 5, 32, 4, "PEITC, Zinc, Magnesium, Astaxanthin, EPA/DHA", 18),
        MealItem("NRO-006", "Neuro-Fuel", "Snack", "Walnut Halves & 90% Dark Single-Origin Cacao", "30g Raw halved walnuts, 20g organic 90% dark chocolate", 280, 7, 8, 24, 5, "Alpha-Linolenic Acid, Epicatechin, Polyphenols", 2),
        // METABOLIC RESET
        MealItem("MET-001", "Metabolic Reset", "Breakfast", "Smoked Salmon & Pastured Butter Omelet with Herbs", "3 Pastured eggs, 80g wild smoked salmon, 1 tbsp grass-fed butter, dill, capers, 1/2 avocado", 510, 36, 2, 38, 4, "Zero insulin spike, Choline, High bioavailability lipids", 12),
        MealItem("MET-002", "Metabolic Reset", "Lunch", "Grass-Fed Ribeye Salad with Gorgonzola & Macadamias", "180g Sliced grilled grass-fed ribeye, 2 cups romaine lettuce, 30g raw macadamia nuts, 20g aged gorgonzola, olive oil", 680, 44, 3, 54, 4, "Palmitoleic acid (Omega-7), CLA, Ketogenic ratio", 15),
        MealItem("MET-003", "Metabolic Reset", "Dinner", "Crispy Skin Duck Breast with Sautéed Asparagus & Ghee", "200g Pan-roasted duck breast, 150g asparagus sautéed in grass-fed ghee, fresh thyme", 620, 42, 4, 48, 4, "Monounsaturated fat profile, Potassium, Low-carb drive", 25),
        MealItem("MET-004", "Metabolic Reset", "Lunch", "Wild Sockeye Salmon Cakes with Lemon Herb Aioli", "200g Wild salmon patties (bound with egg & almond flour), avocado oil mayonnaise, 1 cup steamed broccolini", 560, 46, 3, 40, 4, "High DHA/EPA, Zero blood sugar spike, Sulforaphane", 20),
        MealItem("MET-005", "Metabolic Reset", "Dinner", "Pastured Pork Belly with Crispy Brussels Sprouts & Garlic", "180g Slow-braised crispy pork belly, 1.5 cups roasted Brussels sprouts with Celtic sea salt", 650, 38, 6, 52, 5, "Glucosinolates, High satiety index, Zero refined carbs", 35),
        MealItem("MET-006", "Metabolic Reset", "Snack", "Avocado Halves Filled with Wild Sardines & Olive Oil", "1 Whole avocado halved, 1 can sardines, lemon juice, cracked black pepper", 380, 26, 3, 29, 7, "Zero carb surge, EPA/DHA, Oleic Acid", 3),
        // MICROBIOME SHIELD
        MealItem("MIC-001", "Microbiome Shield", "Breakfast", "Fermented Kimchi & Pastured Egg Golden Scramble", "3 Pastured eggs scrambled in ghee, 1/2 cup unpasteurized artisan kimchi, 1/2 sliced avocado, toasted sesame seeds", 420, 24, 6, 30, 5, "Live lactic acid bacteria (L. plantarum), Capsaicin, Choline", 10),
        MealItem("MIC-002", "Microbiome Shield", "Lunch", "Rainbow Diversity Bowl with Salmon & 7 Plant Varieties", "150g Wild salmon, purple cabbage, shredded carrots, baby spinach, roasted beets, pumpkin seeds, tahini-lemon dressing", 540, 40, 24, 30, 9, "Polyphenols, Beta-Carotene, Betalains, Plant diversity", 20),
        MealItem("MIC-003", "Microbiome Shield", "Dinner", "Prebiotic Sunchoke, Leek & Lentil Slow Stew", "1 cup Cooked French lentils, 100g roasted sunchokes (Jerusalem artichokes), 1 sliced leek, garlic, fresh rosemary, olive oil", 460, 22, 52, 16, 16, "High Inulin fructooligosaccharides, Resistant starch, Kaempferol", 30),
        MealItem("MIC-004", "Microbiome Shield", "Lunch", "Artisan Tempeh Bowl with Raw Sauerkraut & Purple Sweet Potato", "160g Seared fermented tempeh, 1/2 cup raw unpasteurized sauerkraut, 100g steamed purple sweet potato, tahini", 490, 32, 36, 20, 11, "Anthocyanins, Live probiotics, Prebiotic complex fiber", 20),
        MealItem("MIC-005", "Microbiome Shield", "Dinner", "Wild Cod Fillet with Braised Artichokes, Fennel & Capers", "200g Wild Pacific cod, 1 cup globe artichoke hearts, 1 cup braised Florence fennel bulb, capers, extra virgin olive oil", 430, 42, 14, 18, 8, "Anethole, Cynarin, Glutathione, Low FODMAP profile", 25),
        MealItem("MIC-006", "Microbiome Shield", "Snack", "Raw Kefir Smoothie with Green Banana Flour & Blueberries", "250ml Plain goat milk kefir, 1 tbsp green banana flour (resistant starch type 2), 1/2 cup wild blueberries", 260, 14, 22, 9, 7, "Probiotics, Resistant starch prebiotic, Anthocyanins", 3)
    )

    fun getDayPlan(catToken: String): List<MealItem> {
        val types = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        val result = mutableListOf<MealItem>()
        types.forEach { t ->
            val options = meals.filter { it.cat == catToken && it.type == t }
            if (options.isNotEmpty()) {
                result.add(options.random())
            }
        }
        return result
    }
}
