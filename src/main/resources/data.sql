insert into insta_recipe_app.users (id, date_registered, date_updated, email, is_active, last_login, password, profile_picture, role, username)
values  (0xA7C5FEFAD69742DBB50F08CC16ECF796, '2025-02-02 15:48:36.021550', null, 'admin@abv.bg', true, '2025-03-04 15:44:26.046060', '$2a$10$Qep64.WFSmid1HIkJGTPD.CU5/cb8Z2UCSHGo7TCGqXbrF9hFlL6W', '/images/default-profile.png', 'ADMIN', 'admin'),
        (0xAC9A382683F440DA993F0A37F8AEA5B5, '2025-02-02 16:41:42.978594', null, 'thirdTestTest@gmail.com', true, '2025-03-04 15:24:12.971077', '$2a$10$OxC6d4S.B2F9k7RJJGnb9.u8QavMizRgMZQkNiAqOCCuLrUpu1jWq', '/images/default-profile.png', 'ADMIN', 'Third User'),
        (0xD7A6C67D92954F19BF936452A5D1D2F9, '2025-02-02 16:41:29.241018', null, 'second@abv.bg', true, '2025-03-04 15:18:23.783848', '$2a$10$9ysO401eeCoKWl1x8pfDQ.nxvH2lVMo7ruRVBsuhBm6AMttWGbDKy', '/images/default-profile.png', 'USER', 'SecondUser');

insert into insta_recipe_app.categories (id, description, image_url, name)
values  (0x392D87A338B04FBC96DCD4767371532C, 'Desserts are delightful sweet dishes enjoyed at the end of a meal, encompassing a wide variety of flavors, textures, and presentations. This category includes cakes, which range from rich chocolate to light sponge varieties, and pastries, known for their flaky layers and sweet fillings. Frozen desserts like ice cream and sorbet offer refreshing options, while puddings and custards provide creamy indulgence. Other popular types include cookies, tarts, and pies, each with unique ingredients and preparation methods. Desserts not only satisfy sweet cravings but also serve as a centerpiece for celebrations and gatherings.', '/images/desserts.jpg', 'DESSERTS'),
        (0x3D2A28EFC3F041569C8B01161732F52F, 'Snacks are small, convenient food items enjoyed between meals or during social occasions, providing a quick source of energy and satisfaction. They can be categorized into savory options like chips, pretzels, and cheese, as well as sweet treats such as cookies, candies, and fruit. Snacks often include healthy choices like nuts, yogurt, and fresh fruits, catering to diverse dietary preferences. The versatility of snacks allows for endless creativity, from simple combinations to elaborate preparations. Whether for a quick bite on the go or a delightful addition to gatherings, snacks play a significant role in daily eating habits.', '/images/snacks.jpg', 'SNACKS'),
        (0x8EF75EA5ED1C4E2D98E51A9E14CD1CA5, 'Salads are versatile dishes that encompass a wide variety of ingredients and styles, often served as appetizers or side dishes. They can be categorized into several types, including green salads, which feature leafy vegetables; vegetable salads, showcasing a mix of raw or cooked vegetables; and pasta or grain salads, which incorporate starches for added texture. Salads can be tossed, composed, or bound, allowing for diverse presentations and flavor combinations. With options ranging from classic Caesar to refreshing fruit salads, they offer nutritious and colorful additions to any meal, appealing to both health-conscious eaters and culinary enthusiasts alike.', '/images/salads.jpg', 'SALADS'),
        (0xB14FEB7D338A4A2BB6C0A044F71A95E0, 'Appetizers are small, flavorful dishes served before a meal or during social gatherings, designed to stimulate the appetite. Often referred to as hors d''oeuvres, antipasti, or starters, they can range from simple snacks to elaborate presentations. Common types include cocktails, which feature bite-sized seafood or fruits with tangy sauces; hors d''oeuvres, which are highly seasoned and can be served hot or cold; and canapés, small bites with various toppings on a base. Appetizers play a crucial role in culinary traditions worldwide, enhancing dining experiences and encouraging social interaction.', '/images/appetizers.jpg', 'APPETIZERS'),
        (0xD88270CF0A9F42969EDB6735304E7EFC, 'The main course is the centerpiece of a meal, typically following appetizers and preceding dessert. It represents the most substantial dish, often featuring a protein source such as meat, fish, or plant-based alternatives, complemented by various side dishes and sauces. The main course plays a crucial role in providing a satisfying and balanced dining experience, showcasing culinary artistry through thoughtful flavor combinations and presentations. Variations exist across cultures, reflecting regional ingredients and cooking traditions, making the main course a diverse and essential element of global cuisine.', '/images/main-course.jpg', 'MAIN_COURSE'),
        (0xE6B1CF10AA3348018DDC59DB243B923F, 'Vegan recipes encompass a diverse range of plant-based dishes that cater to various tastes and dietary preferences. This category includes everything from hearty one-pot meals and savory stews to fresh salads and baked goods. Vegan cooking emphasizes the use of whole foods, such as fruits, vegetables, grains, legumes, nuts, and seeds, promoting health and sustainability. Popular subcategories include quick weeknight dinners, comfort foods, international cuisines, and seasonal specialties. With endless flavor combinations and creative techniques, vegan recipes offer delicious options for everyone, whether you''re fully vegan or simply exploring plant-based eating.', '/images/vegan.jpg', 'VEGAN'),
        (0xF0151B54713D41AEBC7D1BE11309649D, 'Soups are comforting and versatile dishes that can be enjoyed as a starter, main course, or even a light meal. They come in various forms, including broths, purees, and chunky stews, catering to diverse culinary traditions worldwide. Common ingredients include vegetables, meats, grains, and legumes, often simmered to develop rich flavors. Soups can be served hot or cold, with popular varieties like chicken noodle, minestrone, and gazpacho highlighting their adaptability. Whether nourishing or refreshing, soups provide a warm embrace in every bowl, making them a staple in many cuisines.', '/images/soups.jpg', 'SOUPS'),
        (0xF51612CE46104ACD815D0011C721E893, 'Beverages encompass a wide range of drinks enjoyed for various occasions, classified primarily into two main categories: alcoholic and non-alcoholic. Alcoholic beverages include options like beer, wine, and spirits, each with unique production methods and flavor profiles. Non-alcoholic drinks feature soft drinks, juices, teas, and coffees, catering to diverse tastes and preferences. Additionally, beverages can be categorized by temperature, such as hot or cold drinks, and by function, including functional beverages like energy drinks and health-focused options. This rich variety ensures that there is a beverage for every palate and occasion.', '/images/beverages.jpg', 'BEVERAGES');

insert into insta_recipe_app.recipes (id, cook_time, created_date, description, image, instructions, prep_time, servings, title, updated_date, user_id)
values  (0x0B9B82F1C1A14C2188525FF3C8553E08, 30, '2025-02-02 17:02:22.501365', 'This crowd-pleasing cornbread salad is quite unique but tastes great!', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101132/avzb8heioqzqsurhm0tv.jpg', 'Make the cornbread: Preheat the oven to 375 degrees F (190 degrees C). Spray the bottom and sides of an 8x8-inch pan with cooking spray.Combine cornbread mix and water in a large mixing bowl until smooth; pour into prepared pan.Bake in the preheated oven until a toothpick inserted in the center comes out clean, 30 to 35 minutes. Let cool; crumble and set aside.
Meanwhile, make the salad: Place bacon in a large skillet and cook over medium-high heat, turning occasionally, until evenly browned, about 10 minutes. Drain bacon slices on paper towels.Whisk sour cream, mayonnaise, and dressing mix together in a medium bowl.Crumble 1/2 of the cornbread in the bottom of a large serving dish. Evenly layer with 1 can of pinto beans, 1/2 of the tomatoes, 1/2 cup green bell pepper, 1/2 cup green onions, 1 can of corn, 1 cup of cheese, 1/2 of the bacon, and 1/2 of the sour cream mixture. Repeat the layers. Cover and chill for at least 2 hours before serving.', 20, 12, 'Cornbread Salad', null, 0xA7C5FEFAD69742DBB50F08CC16ECF796),
        (0x14F0CA6DA156496FA0928549174136A0, 10, '2025-02-02 17:09:05.637866', 'A simple vinaigrette graces this lovely salad topped with pomegranate arils, looking like little jewels. Add sweet, fresh red pear slices, some pungent Gorgonzola crumbles, a few lightly toasted walnuts and chopped scallion to a bed of greens, and you''ll be enjoying this salad in about 25 minutes!', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101212/o2trntpnkl6tkbmuqlqa.jpg', 'Combine apple cider vinegar, extra-virgin olive oil, honey, Dijon mustard, salt, and white pepper in a small container with a lid. Cover tightly and shake until well combined. Set aside.
Place lettuce in a salad bowl. Sprinkle with toasted walnut pieces and cheese crumbles. Fan pear slices on the top of the salad and sprinkle with pomegranate arils and chopped scallion.
Drizzle with vinaigrette and serve.', 25, 6, 'Pomegranate-Gorgonzola Salad', null, 0xA7C5FEFAD69742DBB50F08CC16ECF796),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, 5, '2025-02-02 19:23:22.545988', 'This eggnog martini, with eggnog, vodka, and Irish cream, is creamy and delicious, a perfect cocktail for holiday parties.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101930/mrckowl9jeaeujosfksg.jpg', 'Pour brown sugar onto a plate. Pour a splash of eggnog on another plate and dip the rim of the glass in eggnog and then in brown sugar.
Fill a cocktail shaker with ice. Add eggnog, vodka, and Baileys. Close the shaker and shake until the outside of shaker is frosted, about 30 seconds. Pour into the prepared glass and sprinkle with cinnamon.', null, 1, 'Eggnog Martini', null, 0xAC9A382683F440DA993F0A37F8AEA5B5),
        (0x7F38C28279824D3199120441DD8493F1, 15, '2025-02-02 16:49:38.840702', 'This bulgogi chicken is a quick-and-easy, but very tasty, meal. You can substitute the chicken with beef or pork for variety. My wife and I absolutely love this recipe, and it only takes 30 minutes with very few dirty dishes! Serve over rice.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101285/bi4h3ikjr7pfyk3lst3d.jpg', 'Whisk onion, soy sauce, brown sugar, garlic, sesame oil, sesame seeds, cayenne pepper, salt, and black pepper together in a bowl until marinade is smooth.Cook and stir chicken and marinade together in a large skillet over medium-high heat until chicken is cooked through, about 15 minutes.', 15, 4, 'Skillet Chicken Bulgogi', null, 0xA7C5FEFAD69742DBB50F08CC16ECF796),
        (0x97C0AB3BCF89411193E832D040E6794C, 450, '2025-02-02 19:12:06.929352', 'This is as close as one can get to the famous restaurant ''bar scheez; spread my mother loved, Win Schuler''s®. It became a staple in our house. The horseradish gives the spread that interesting bite. You''ll have people begging you for the recipe and you won''t be able to stop eating it! You can start with less horseradish and increase to your taste. Serve with crackers, pita or bagel chips, or pretzels. Even great with celery sticks! Store leftovers in refrigerator in airtight container.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101749/xjlfljaeuxlkfxfstw3k.jpg', 'Mix cheese spread, horseradish, Worcestershire sauce, dry mustard, and garlic powder in a mixing bowl.
Refrigerate 8 hours to overnight.', 10, 20, 'Original Bar Cheese Spread', null, 0xD7A6C67D92954F19BF936452A5D1D2F9),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, 75, '2025-02-02 16:58:23.744283', 'American goulash was one of my all-time favorite comfort food meals when I was growing up. They served it in my school cafeteria alongside a slice of buttered white bread and a carton of milk. This Americanized version of goulash was invented to stretch a small amount of beef into enough food for a not-so-small family. It''s a simple dish that doesn''t taste simple, so it''s perfect for your weeknight dinner rotation.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101335/yx8zrrcn6uw3ctspqrpn.jpg', 'Heat oil in a pot over medium-high heat. Add ground beef and onion; cook and stir until beef is browned and crumbly and onion is translucent, about 5 minutes. Continue to cook and stir until liquid is evaporated, 3 to 5 minutes. Add garlic, bay leaves, paprika, Italian seasoning, salt, black pepper, and cayenne.Cook, stirring occasionally, until flavors come together, about 3 minutes. Pour in broth, marinara sauce, and diced tomatoes. Pour water into the sauce jar, swirl, and pour into the pot. Stir in soy sauce; bring to a simmer. Reduce the heat to medium and simmer until flavors intensify, about 30 minutes.Increase the heat to medium-high and bring to a rapid simmer. Stir in macaroni; cook, stirring occasionally, until just barely tender, about 12 minutes. Check for doneness after 10 minutes.Remove from the heat and discard bay leaves. Stir in Cheddar and parsley. Cover and let rest for 5 minutes. Taste and season as desired.', 15, 12, 'Chef John''s American Goulash', null, 0xA7C5FEFAD69742DBB50F08CC16ECF796),
        (0xDD3C6024B1214BB9836F41ABBE71138C, 35, '2025-02-02 19:17:59.341201', 'This creamy tomato bean soup pairs nicely with warm, crusty bread or a light side salad. The cannellini beans provide a creamy texture and add protein to this soup for extra staying power.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101973/b6qaq1doyq2dyr5nphbj.jpg', 'Preheat the oven to 375 degrees F (190 degrees C). Line a baking tray with foil.
Place tomatoes and peppers on the baking tray. Drizzle with olive oil and sprinkle with Cantanzaro herbs and garlic granules.
Bake in the preheated oven until vegetables are tender, about 30 minutes.
Add beans, roasted vegetables, tomato paste, and chicken broth to the jar of a blender and blend until smooth. Heat pureed soup over medium-low heat in a saucepan until hot, about 5 minutes; remove from heat. Season with salt and pepper.
Garnish each bowl with thyme, drizzle with heavy cream, and serve immediately.', 10, 4, 'Creamy Tomato Bean Soup', null, 0xAC9A382683F440DA993F0A37F8AEA5B5),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, 25, '2025-02-02 17:13:33.794644', 'We have these sausage-stuffed mushrooms at most of our neighborhood gatherings. They''re cheesy, delicious, and always the hit of the party.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101785/pdtbhnsxot3nnpdqz14x.jpg', 'Preheat the oven to 350 degrees F (175 degrees C).
Hollow out each mushroom cap, reserving the scrapings.
Heat a skillet over medium-high heat; add sausage, onion, and reserved mushroom scrapings. Cook and stir until sausage is browned and cooked through, 4 to 6 minutes. Drain and discard grease; return sausage mixture to the skillet.Stir 3 ounces Parmesan cheese, bread crumbs, garlic, and parsley into sausage mixture. Cook and stir until heated through, 3 to 5 minutes.Stuff each mushroom cap with sausage mixture and place on a baking sheet.Bake stuffed mushrooms in the preheated oven for 12 minutes. Sprinkle remaining 1 ounce Parmesan cheese over mushrooms and bake until mushrooms are cooked through and cheese is melted and bubbling, about 3 minutes.', 20, 24, 'Sausage-Stuffed Mushrooms', null, 0xD7A6C67D92954F19BF936452A5D1D2F9),
        (0xE5A93B3135D44E688A1BC296952DEB69, 120, '2025-02-02 19:07:58.555784', 'This sweet, Southern-inspired cheese ball channels all the best flavors of a classic pecan pie—from the crunchy pecan coating to the gooey, caramel center. The cream cheese base is enriched with vanilla pudding mix to create a smooth, almost cookie dough-like texture that’s perfect for scooping with apple slices, graham crackers, or pretzel chips. This sweet, decadent spin on a traditionally savory appetizer is sure to impress every guest at your holiday table.
“The cheese ball on its own is delicious, but who doesn’t love a caramel center!” said recipe cross-tester Amanda Holstein.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741101825/aflty4jq21vmrsqqzb4i.jpg', 'Stir together caramel dip and 1 tablespoon of the pecans in a small bowl; store in refrigerator until ready to use.Beat cream cheese and butter with a stand mixer fitted with a paddle attachment on high speed until smooth, about 1 minute, stopping to scrape down sides of bowl as necessary. Add powdered sugar and brown sugar; beat on low until just combined, about 5 seconds; increase speed to medium-high and beat until smooth, about 30 seconds, scraping down sides of bowl as necessary. Add pudding mix, lemon juice, salt, cinnamon, and 1/3 cup of the pecans; mix until just combined, about 10 seconds.Line the inside of a medium bowl with plastic wrap, leaving about a 2-inch overhang around the bowl. Place cream cheese mixture into prepared bowl, pressing down until cream cheese mixture is firmly packed; make a well in the center, about 3 inches wide and 2 inches deep. Refrigerate, uncovered, until just firm, about 45 minutes.Spoon caramel mixture into the well of the chilled cream cheese mixture. Use the edges of the plastic wrap to help spread cheese evenly over caramel mixture to enclose, pressing together the edges to create a sealed ball with a caramel center. Unwrap and smooth outside of cheese with clean fingers or the back of a spoon until smooth and no cracks remain.Place ball onto a new piece of plastic wrap and wrap it well, reforming it into a ball shape, about 5 inches in diameter.', 15, 12, 'Pecan Pie Cheese Ball', null, 0xD7A6C67D92954F19BF936452A5D1D2F9),
        (0xFADAA53C465D49EAA53610248147C872, 5, '2025-02-02 19:20:28.008006', 'This peach whiskey sour is a simple peach schnapps and whiskey cocktail with a little lemon tartness.', 'https://res.cloudinary.com/dgvcwy7qu/image/upload/v1741102065/dn62ubcm6a5syvewoewy.jpg', 'Scoop ice into a shaker. Pour in whiskey, peach schnapps, and simple syrup. Squeeze in the juice of 1 lemon. Cover shaker, and shake until the outside of shaker is frosted, about 30 seconds. Pour drink into a glass. Serve.', null, 1, 'Peach Whiskey Sour', null, 0xAC9A382683F440DA993F0A37F8AEA5B5);

insert into insta_recipe_app.recipes_categories (recipe_id, category_id)
values  (0x0B9B82F1C1A14C2188525FF3C8553E08, 0xE6B1CF10AA3348018DDC59DB243B923F),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, 0x8EF75EA5ED1C4E2D98E51A9E14CD1CA5),
        (0x14F0CA6DA156496FA0928549174136A0, 0x8EF75EA5ED1C4E2D98E51A9E14CD1CA5),
        (0x7F38C28279824D3199120441DD8493F1, 0xD88270CF0A9F42969EDB6735304E7EFC),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, 0xD88270CF0A9F42969EDB6735304E7EFC),
        (0x97C0AB3BCF89411193E832D040E6794C, 0xB14FEB7D338A4A2BB6C0A044F71A95E0),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, 0xB14FEB7D338A4A2BB6C0A044F71A95E0),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, 0x3D2A28EFC3F041569C8B01161732F52F),
        (0xE5A93B3135D44E688A1BC296952DEB69, 0xB14FEB7D338A4A2BB6C0A044F71A95E0),
        (0xE5A93B3135D44E688A1BC296952DEB69, 0x3D2A28EFC3F041569C8B01161732F52F),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, 0xF51612CE46104ACD815D0011C721E893),
        (0xDD3C6024B1214BB9836F41ABBE71138C, 0xE6B1CF10AA3348018DDC59DB243B923F),
        (0xDD3C6024B1214BB9836F41ABBE71138C, 0xF0151B54713D41AEBC7D1BE11309649D),
        (0xFADAA53C465D49EAA53610248147C872, 0xE6B1CF10AA3348018DDC59DB243B923F),
        (0xFADAA53C465D49EAA53610248147C872, 0xF51612CE46104ACD815D0011C721E893);

insert into insta_recipe_app.recipes_ingredients (recipe_id, ingredients)
values  (0xB914924CC79F447C8C3D8972D9D3FF5C, '1 tablespoon olive oil'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '2 pounds ground beef'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '1 large onion diced'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '4 cloves garlic minced'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '2 large bay leaves'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '2 tablespoons Hungarian paprika'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '2 teaspoons Italian seasoning'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '2 teaspoons kosher salt'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '½ teaspoon ground black pepper'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, 'a pinch cayenne pepper or to taste (note: "a" is added for clarity since "pinch" is not quantified)'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '1 quart chicken broth or water'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, 'a 24-ounce jar marinara sauce'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, 'a 15-ounce can diced tomatoes'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '1 cup water (corrected to "cup" for consistency; however it should be specified as "1 cup")'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '2 tablespoons soy sauce'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, '2 cups elbow macaroni'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, 'a quarter cup chopped Italian parsley (corrected for clarity)'),
        (0xB914924CC79F447C8C3D8972D9D3FF5C, 'a cup shredded white Cheddar cheese (Optional).'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, '1 tablespoon olive oil'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, 'pounds ground beef'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, 'large onion diced'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, '4 cloves garlic minced'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, 'large bay leaves/tablespoons Hungarian paprika/teaspoons Italian seasoning/teaspoons kosher salt'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, '3/4 teaspoon ground black pepper'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, 'pinch cayenne pepper/quart chicken broth'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, '(24 ounce) jar marinara sauce'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, '(15 ounce) can diced tomatoes'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, 'cup water/tablespoons soy sauce/cups elbow macaroni'),
        (0x0B9B82F1C1A14C2188525FF3C8553E08, '2/3 cup chopped Italian parsley/cup shredded white Cheddar cheese(Optional)'),
        (0x7F38C28279824D3199120441DD8493F1, '¼ cup chopped onion'),
        (0x7F38C28279824D3199120441DD8493F1, '5 tablespoons soy sauce'),
        (0x7F38C28279824D3199120441DD8493F1, '2 ½ tablespoons brown sugar'),
        (0x7F38C28279824D3199120441DD8493F1, '2 tablespoons minced garlic'),
        (0x7F38C28279824D3199120441DD8493F1, '2 tablespoons sesame oil'),
        (0x7F38C28279824D3199120441DD8493F1, '1 tablespoon sesame seeds'),
        (0x7F38C28279824D3199120441DD8493F1, '½ teaspoon cayenne'),
        (0x7F38C28279824D3199120441DD8493F1, 'salt and ground black pepper to taste'),
        (0x7F38C28279824D3199120441DD8493F1, '1 pound skinless and boneless chicken breasts-cut into thin strips'),
        (0x14F0CA6DA156496FA0928549174136A0, '3 tablespoons apple cider vinegar'),
        (0x14F0CA6DA156496FA0928549174136A0, '2 tablespoons extra-virgin olive oil'),
        (0x14F0CA6DA156496FA0928549174136A0, '1 tablespoon honey'),
        (0x14F0CA6DA156496FA0928549174136A0, '1 teaspoon Dijon mustard'),
        (0x14F0CA6DA156496FA0928549174136A0, '1 pinch salt'),
        (0x14F0CA6DA156496FA0928549174136A0, '1 pinch ground white pepper'),
        (0x14F0CA6DA156496FA0928549174136A0, '6 cups romaine lettuce - torn washed and dried'),
        (0x14F0CA6DA156496FA0928549174136A0, '⅓ cup chopped toasted walnuts'),
        (0x14F0CA6DA156496FA0928549174136A0, '¼ cup crumbled Gorgonzola cheese'),
        (0x14F0CA6DA156496FA0928549174136A0, '1 red pear cored and sliced'),
        (0x14F0CA6DA156496FA0928549174136A0, '¼ cup pomegranate arils'),
        (0x14F0CA6DA156496FA0928549174136A0, '1 green onion-chopped'),
        (0x97C0AB3BCF89411193E832D040E6794C, '1 (15 ounce) jar pasteurized process cheese spread (such as Cheez Whiz®)'),
        (0x97C0AB3BCF89411193E832D040E6794C, '⅓ cup prepared horseradish or to taste'),
        (0x97C0AB3BCF89411193E832D040E6794C, '1 teaspoon Worcestershire sauce'),
        (0x97C0AB3BCF89411193E832D040E6794C, '½ teaspoon dry mustard'),
        (0x97C0AB3BCF89411193E832D040E6794C, '¼ teaspoon garlic powder or more to taste'),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, '24 large mushrooms stems removed'),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, '1 pound bulk hot Italian sausage'),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, '1 onion diced'),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, '4 ounces grated Parmesan cheese divided'),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, '¼ cup Italian bread crumbs'),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, '1 teaspoon minced garlic'),
        (0xDFF2C67E0C7945C5ABE2FC24149B900C, '1 teaspoon chopped fresh parsley'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '2 packages caramel dip singles (such as Marzetti) plus more for drizzling'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '1 tablespoon finely chopped toasted pecans'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '2 (8 ounce) packages cream cheese'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '1/2 cup unsalted butter'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '1/2 cup powdered sugar'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '2 tablespoons light brown sugar'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '1 (3.2 ounce) package instant vanilla pudding mix'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '2 teaspoons fresh lemon juice'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '1/2 teaspoon ground cinnamon'),
        (0xE5A93B3135D44E688A1BC296952DEB69, '2/3 cup finely chopped pecans divided '),
        (0xE5A93B3135D44E688A1BC296952DEB69, '1/4 teaspoon kosher salt/apple slices graham crackers and pretzel chips for dipping'),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, '1 teaspoon brown sugar or as needed'),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, '4 fluid ounces eggnog'),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, '1 1/2 fluid ounce vodka'),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, '1 1/2 fluid ounce Irish cream liqueur such as Bailey''s®'),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, 'sprinkle of ground cinnamon'),
        (0x526E11FBA8D542A08C03EEA8DD782F1F, 'cinnamon stick for garnish (optional)'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '4 Roma tomatoes quartered'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1/2 red bell pepper sliced'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1 tablespoon olive oil'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1 1/2 teaspoons Cantanzaro herbs'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1/2 teaspoon granulated garlic'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1 (15.5-ounce) can cannellini beans'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1 tablespoon tomato paste'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1 cup chicken broth'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, '1/4 cup heavy cream plus more for drizzling'),
        (0xDD3C6024B1214BB9836F41ABBE71138C, 'salt and freshly ground black pepper to taste/thyme sprigs for garnish'),
        (0xFADAA53C465D49EAA53610248147C872, 'ice-as needed'),
        (0xFADAA53C465D49EAA53610248147C872, '1 1/2 fluid ounces whiskey'),
        (0xFADAA53C465D49EAA53610248147C872, '1 1/2 fluid ounces peach schnapps'),
        (0xFADAA53C465D49EAA53610248147C872, '1/2 fluid ounces simple syrup'),
        (0xFADAA53C465D49EAA53610248147C872, '1 lemon');

insert into insta_recipe_app.comments (id, content, created_date, user_id, recipe_id)
values  (0x2E1F12FA3A664F4DB258B341372B5872, 'Such a warm and nostalgic description! I love how you highlight its comforting and budget-friendly nature. You might consider suggesting an optional topping, like shredded cheese or fresh parsley, to add a little extra flair. Perfect for a cozy family meal!', '2025-03-27 13:57:45.261154', 0xAC9A382683F440DA993F0A37F8AEA5B5, 0xB914924CC79F447C8C3D8972D9D3FF5C),
        (0x4DDBCB2F19D444DFAC5B965A5EEC3425, 'Refreshing and flavorful twist on a classic whiskey sour! You might consider suggesting a garnish, like a peach slice or a cherry, to enhance the presentation. Perfect for a summer evening or a cozy night in!', '2025-03-27 13:59:22.470870', 0xAC9A382683F440DA993F0A37F8AEA5B5, 0xFADAA53C465D49EAA53610248147C872),
        (0x50E1DCF26D8D4A4D835383FC2EB3900A, 'This recipe sounds absolutely delicious! I love how you''ve captured the nostalgia and the unique kick from the horseradish.', '2025-03-27 13:46:42.326806', 0xA7C5FEFAD69742DBB50F08CC16ECF796, 0x97C0AB3BCF89411193E832D040E6794C),
        (0x63C261C3A28F4FCD902F345A5489942B, 'The imagery of pomegranate arils as "little jewels" is especially lovely. You might consider suggesting a specific type of greens, like arugula or spinach, to enhance the flavor profile. Sounds like a perfect balance of sweet, tangy, and crunchy!', '2025-03-27 13:58:23.944847', 0xAC9A382683F440DA993F0A37F8AEA5B5, 0x14F0CA6DA156496FA0928549174136A0),
        (0x745D3FF6574D44FBBB42B7B1720D4B17, 'This sounds like a fantastic and flavorful meal! I love how it''s both quick and versatile. You might consider mentioning a suggested side, like kimchi or steamed veggies, to complement the dish. Great for busy weeknights!', '2025-03-27 13:53:19.995509', 0xD7A6C67D92954F19BF936452A5D1D2F9, 0x7F38C28279824D3199120441DD8493F1),
        (0x907D0A91515E4E0BA6C61DE555D8FC83, 'Thanks for the comment!', '2025-03-27 13:49:17.947562', 0xD7A6C67D92954F19BF936452A5D1D2F9, 0x97C0AB3BCF89411193E832D040E6794C),
        (0xF4884BF0148347CD93A2684313B8991D, 'This sounds like a festive and indulgent holiday treat! You might consider adding a garnish suggestion, like a sprinkle of nutmeg or a cinnamon stick, to enhance the presentation and flavor. Perfect for cozy celebrations!', '2025-03-27 13:48:01.682897', 0xA7C5FEFAD69742DBB50F08CC16ECF796, 0x526E11FBA8D542A08C03EEA8DD782F1F);