package com.github.foli_backend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.constant.StoreStatusEnum;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.entity.FmProductCategory;
import com.github.foli_backend.entity.FmProductImage;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.mapper.FmBalanceLogMapper;
import com.github.foli_backend.mapper.FmCartItemMapper;
import com.github.foli_backend.mapper.FmComplaintMapper;
import com.github.foli_backend.mapper.FmMessageMapper;
import com.github.foli_backend.mapper.FmOrderItemMapper;
import com.github.foli_backend.mapper.FmOrderMapper;
import com.github.foli_backend.mapper.FmProductCategoryMapper;
import com.github.foli_backend.mapper.FmProductImageMapper;
import com.github.foli_backend.mapper.FmProductMapper;
import com.github.foli_backend.mapper.FmReturnRefundMapper;
import com.github.foli_backend.mapper.FmStoreMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 数据初始化器 — 在应用启动时自动插入种子数据
 * Data initializer — seeds demo data on application startup.
 * <p>
 * 如果 fm_user 表中已有数据则跳过（避免重复初始化）。
 * Skips seeding when fm_user already contains rows to prevent duplicates.
 */
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // ==================== 12 Mapper 接口注入 / 12 mapper injections ====================
    private final FmUserMapper userMapper;
    private final FmStoreMapper storeMapper;
    private final FmProductCategoryMapper categoryMapper;
    private final FmProductMapper productMapper;
    private final FmProductImageMapper productImageMapper;
    private final FmCartItemMapper cartItemMapper;
    private final FmOrderMapper orderMapper;
    private final FmOrderItemMapper orderItemMapper;
    private final FmMessageMapper messageMapper;
    private final FmComplaintMapper complaintMapper;
    private final FmReturnRefundMapper returnRefundMapper;
    private final FmBalanceLogMapper balanceLogMapper;

    public DataInitializer(FmUserMapper userMapper,
                           FmStoreMapper storeMapper,
                           FmProductCategoryMapper categoryMapper,
                           FmProductMapper productMapper,
                           FmProductImageMapper productImageMapper,
                           FmCartItemMapper cartItemMapper,
                           FmOrderMapper orderMapper,
                           FmOrderItemMapper orderItemMapper,
                           FmMessageMapper messageMapper,
                           FmComplaintMapper complaintMapper,
                           FmReturnRefundMapper returnRefundMapper,
                           FmBalanceLogMapper balanceLogMapper) {
        this.userMapper = userMapper;
        this.storeMapper = storeMapper;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.cartItemMapper = cartItemMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.messageMapper = messageMapper;
        this.complaintMapper = complaintMapper;
        this.returnRefundMapper = returnRefundMapper;
        this.balanceLogMapper = balanceLogMapper;
    }

    @Override
    public void run(String... args) {
        // 检查是否已有数据，避免重复初始化
        // Check if data already exists to avoid duplicate seeding
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<>());
        if (userCount > 0) {
            log.info("===== 数据库已有 {} 个用户，跳过种子数据初始化 / DB already has {} users, skip seeding =====", userCount, userCount);
            return;
        }

        log.info("===== 开始初始化种子数据 / Start seeding initial data =====");

        // ---- 第 1 步：创建8个用户 / Step 1: Seed 8 users ----
        log.info("[1/4] 创建用户 / Seeding users...");
        Long adminId = createUser("admin", "admin123", "系统管理员", "13800000001", "admin@folimall.com", RoleConstants.ADMIN, new BigDecimal("99999.00"));
        Long seller01Id = createUser("seller01", "seller123", "数码达人", "13800000002", "seller01@folimall.com", RoleConstants.SELLER, new BigDecimal("50000.00"));
        Long seller02Id = createUser("seller02", "seller123", "时尚掌柜", "13800000003", "seller02@folimall.com", RoleConstants.SELLER, new BigDecimal("50000.00"));
        Long seller03Id = createUser("seller03", "seller123", "美食专家", "13800000004", "seller03@folimall.com", RoleConstants.SELLER, new BigDecimal("50000.00"));
        Long seller04Id = createUser("seller04", "seller123", "家居买手", "13800000005", "seller04@folimall.com", RoleConstants.SELLER, new BigDecimal("50000.00"));
        Long seller05Id = createUser("seller05", "seller123", "运动达人", "13800000006", "seller05@folimall.com", RoleConstants.SELLER, new BigDecimal("50000.00"));
        Long buyer01Id = createUser("buyer01", "buyer123", "小明", "13800000007", "buyer01@folimall.com", RoleConstants.BUYER, new BigDecimal("10000.00"));
        Long buyer02Id = createUser("buyer02", "buyer123", "小红", "13800000008", "buyer02@folimall.com", RoleConstants.BUYER, new BigDecimal("10000.00"));
        log.info("  用户创建完毕 / Users created: admin={}, seller01={}, seller02={}, seller03={}, seller04={}, seller05={}, buyer01={}, buyer02={}",
                adminId, seller01Id, seller02Id, seller03Id, seller04Id, seller05Id, buyer01Id, buyer02Id);

        // ---- 第 2 步：创建分类树 / Step 2: Seed category tree ----
        log.info("[2/4] 创建分类 / Seeding categories...");
        // 一级分类 / Top-level categories (parentId=0)
        Long catElectronics = createCategory(0L, "数码电子 / Electronics", 1);
        Long catClothing = createCategory(0L, "服装鞋帽 / Clothing & Shoes", 2);
        Long catFood = createCategory(0L, "食品饮料 / Food & Beverages", 3);
        Long catHome = createCategory(0L, "家居生活 / Home & Living", 4);
        Long catSports = createCategory(0L, "运动户外 / Sports & Outdoors", 5);

        // 二级分类 / Sub-categories — 数码电子 / Electronics
        Long catPhones = createCategory(catElectronics, "手机 / Phones", 1);
        Long catComputers = createCategory(catElectronics, "电脑 / Computers", 2);
        Long catHeadphones = createCategory(catElectronics, "耳机 / Headphones", 3);
        Long catCameras = createCategory(catElectronics, "相机 / Cameras", 4);

        // 二级分类 / Sub-categories — 服装鞋帽 / Clothing & Shoes
        Long catMenClothing = createCategory(catClothing, "男装 / Men's Clothing", 1);
        Long catWomenClothing = createCategory(catClothing, "女装 / Women's Clothing", 2);
        Long catShoes = createCategory(catClothing, "鞋子 / Shoes", 3);
        Long catAccessories = createCategory(catClothing, "配饰 / Accessories", 4);

        // 二级分类 / Sub-categories — 食品饮料 / Food & Beverages
        Long catSnacks = createCategory(catFood, "零食 / Snacks", 1);
        Long catBeverages = createCategory(catFood, "饮品 / Beverages", 2);
        Long catFreshFood = createCategory(catFood, "生鲜 / Fresh Food", 3);

        // 二级分类 / Sub-categories — 家居生活 / Home & Living
        Long catFurniture = createCategory(catHome, "家具 / Furniture", 1);
        Long catKitchenware = createCategory(catHome, "厨具 / Kitchenware", 2);
        Long catStorage = createCategory(catHome, "收纳 / Storage", 3);

        // 二级分类 / Sub-categories — 运动户外 / Sports & Outdoors
        Long catSportswear = createCategory(catSports, "运动鞋服 / Sportswear", 1);
        Long catFitness = createCategory(catSports, "健身器材 / Fitness Equipment", 2);
        Long catOutdoor = createCategory(catSports, "户外装备 / Outdoor Gear", 3);
        log.info("  分类创建完毕 / Categories created: 5 一级 + 17 二级 = 22 total");

        // ---- 第 3 步：创建5个店铺 / Step 3: Seed 5 stores ----
        log.info("[3/4] 创建店铺 / Seeding stores...");
        Long store1Id = createStore(seller01Id, "数码先锋店 / Digital Pioneer Store", "最新数码产品，品质保证 / Latest digital products, quality guaranteed");
        Long store2Id = createStore(seller02Id, "时尚衣橱 / Fashion Wardrobe", "潮流服饰，穿出你的风格 / Trendy fashion, express your style");
        Long store3Id = createStore(seller03Id, "美味食光 / Delicious Time", "精选美食，舌尖上的享受 / Premium food, a feast for your taste buds");
        Long store4Id = createStore(seller04Id, "温馨家居 / Cozy Home", "打造理想家居生活 / Creating your ideal home living");
        Long store5Id = createStore(seller05Id, "活力运动馆 / Vitality Sports", "运动装备，激发无限活力 / Sports gear, unleash your vitality");
        log.info("  店铺创建完毕 / Stores created: store1={}, store2={}, store3={}, store4={}, store5={}",
                store1Id, store2Id, store3Id, store4Id, store5Id);

        // ---- 第 4 步：创建60个商品 + 商品图片 / Step 4: Seed 60 products + images ----
        log.info("[4/4] 创建商品 / Seeding products...");

        // ========== Store 1 - 数码先锋店 / Digital Pioneer ==========
        Long p01 = createProduct(store1Id, catPhones, "iPhone 15 Pro Max 256GB", "Apple旗舰手机，钛金属设计 / Apple flagship phone, titanium design", "01", 4999.00, 50, 128);
        Long p02 = createProduct(store1Id, catPhones, "Samsung Galaxy S25 Ultra", "三星机皇，AI智能体验 / Samsung king, AI smart experience", "02", 4599.00, 30, 96);
        Long p03 = createProduct(store1Id, catComputers, "MacBook Pro 14寸 M3 Pro", "苹果专业笔记本，M3 Pro芯片 / Apple pro laptop, M3 Pro chip", "03", 7999.00, 20, 45);
        Long p04 = createProduct(store1Id, catComputers, "ThinkPad X1 Carbon Gen 12", "商务轻薄本，14寸2.8K屏 / Business ultrabook, 14\" 2.8K display", "04", 6599.00, 15, 32);
        Long p05 = createProduct(store1Id, catHeadphones, "Sony WH-1000XM5 降噪耳机", "行业标杆降噪，30小时续航 / Industry-leading ANC, 30h battery", "05", 1299.00, 80, 0);
        Long p06 = createProduct(store1Id, catHeadphones, "Apple AirPods Pro 2nd Gen", "自适应降噪，H2芯片 / Adaptive ANC, H2 chip", "06", 899.00, 100, 0);
        Long p07 = createProduct(store1Id, catCameras, "Sony A7M4 全画幅微单", "3300万像素，专业摄影 / 33MP full-frame, pro photography", "07", 8999.00, 10, 0);
        Long p08 = createProduct(store1Id, catCameras, "Canon EOS R6 Mark II", "全画幅专微，高速连拍 / Full-frame mirrorless, high-speed burst", "08", 7899.00, 8, 5);
        Long p09 = createProduct(store1Id, catPhones, "Xiaomi 14 Ultra 512GB", "徕卡光学，骁龙8Gen3 / Leica optics, Snapdragon 8 Gen 3", "09", 3299.00, 40, 167);
        Long p10 = createProduct(store1Id, catComputers, "iPad Air M2 11寸", "M2芯片，轻薄便携 / M2 chip, thin and portable", "10", 2799.00, 25, 0);
        Long p11 = createProduct(store1Id, catHeadphones, "Bose QC45 降噪耳机", "Bose经典降噪，舒适佩戴 / Bose classic ANC, comfortable fit", "11", 1099.00, 60, 23);
        Long p12 = createProduct(store1Id, catCameras, "DJI Mini 4 Pro 无人机", "249克轻巧航拍，4K/100fps / 249g mini drone, 4K/100fps", "12", 3499.00, 12, 0);

        // ========== Store 2 - 时尚衣橱 / Fashion Wardrobe ==========
        Long p13 = createProduct(store2Id, catMenClothing, "男士商务休闲夹克", "百搭立领，商务休闲两穿 / Versatile stand collar jacket", "13", 399.00, 150, 0);
        Long p14 = createProduct(store2Id, catMenClothing, "纯棉修身长袖衬衫", "新疆长绒棉，免烫抗皱 / Xinjiang long-staple cotton, wrinkle-free", "14", 199.00, 200, 89);
        Long p15 = createProduct(store2Id, catWomenClothing, "法式复古碎花连衣裙", "优雅碎花，显瘦A字裙摆 / French floral, slimming A-line", "15", 299.00, 120, 56);
        Long p16 = createProduct(store2Id, catWomenClothing, "宽松显瘦针织开衫", "柔软亲肤，慵懒风 / Soft skin-friendly knit, relaxed style", "16", 259.00, 80, 0);
        Long p17 = createProduct(store2Id, catShoes, "Nike Air Max 270 运动鞋", "大气垫缓震，潮流百搭 / Big Air cushion, trendy all-match", "17", 599.00, 60, 0);
        Long p18 = createProduct(store2Id, catShoes, "Adidas Ultraboost 跑鞋", "Boost缓震科技，袜套式贴合 / Boost cushioning, sock-like fit", "18", 699.00, 45, 0);
        Long p19 = createProduct(store2Id, catAccessories, "真皮手工腰带", "头层牛皮，简约针扣 / Genuine leather, classic pin buckle", "19", 159.00, 300, 72);
        Long p20 = createProduct(store2Id, catAccessories, "时尚太阳镜", "偏光镜片，防UV400 / Polarized lenses, UV400 protection", "20", 129.00, 250, 0);
        Long p21 = createProduct(store2Id, catAccessories, "羊绒围巾 冬季保暖", "内蒙古羊绒，柔软保暖 / Inner Mongolia cashmere, warm & soft", "21", 199.00, 180, 0);
        Long p22 = createProduct(store2Id, catMenClothing, "男士休闲西裤", "弹力免烫，商务通勤 / Stretch wrinkle-free, business commute", "22", 349.00, 90, 0);
        Long p23 = createProduct(store2Id, catWomenClothing, "小香风粗花呢外套", "经典编织，名媛气质 / Classic tweed, elegant ladylike", "23", 599.00, 35, 0);
        Long p24 = createProduct(store2Id, catShoes, "Converse Chuck 70 帆布鞋", "复古经典，厚底增高 / Retro classic, platform sole", "24", 399.00, 100, 0);

        // ========== Store 3 - 美味食光 / Delicious Time ==========
        Long p25 = createProduct(store3Id, catSnacks, "手工牛肉干 麻辣味 500g", "精选牛后腿肉，传统工艺 / Premium beef hind leg, traditional recipe", "25", 39.90, 500, 314);
        Long p26 = createProduct(store3Id, catSnacks, "进口混合坚果 每日坚果 750g", "6种坚果果干，科学配比 / 6 kinds of nuts & dried fruit, balanced blend", "26", 69.90, 300, 0);
        Long p27 = createProduct(store3Id, catBeverages, "有机绿茶 明前龙井 250g", "杭州西湖产区，手工炒制 / West Lake Hangzhou, hand-roasted", "27", 99.00, 200, 67);
        Long p28 = createProduct(store3Id, catBeverages, "哥伦比亚手冲咖啡豆 500g", "单一产区，中深烘焙 / Single origin, medium-dark roast", "28", 79.00, 150, 0);
        Long p29 = createProduct(store3Id, catFreshFood, "新鲜进口车厘子 2kg", "智利空运，JJ级大果 / Chile air-freight, JJ grade large fruit", "29", 128.00, 50, 0);
        Long p30 = createProduct(store3Id, catFreshFood, "澳洲安格斯牛排 250g×4", "谷饲200天，雪花纹理 / Grain-fed 200 days, marbled texture", "30", 168.00, 30, 0);
        Long p31 = createProduct(store3Id, catSnacks, "手工蛋黄酥 红豆味 12枚", "酥到掉渣，低糖配方 / Flaky crust, reduced sugar recipe", "31", 49.90, 400, 0);
        Long p32 = createProduct(store3Id, catBeverages, "云南普洱茶 熟茶饼 357g", "勐海古树原料，陈香醇厚 / Menghai ancient tree, mellow aged aroma", "32", 159.00, 100, 0);
        Long p33 = createProduct(store3Id, catSnacks, "精选混合果干 500g", "自然风干，无添加糖 / Naturally dried, no added sugar", "33", 29.90, 600, 203);
        Long p34 = createProduct(store3Id, catFreshFood, "新西兰奇异果 12个装", "阳光金果，维C满满 / SunGold kiwifruit, rich in vitamin C", "34", 59.90, 80, 0);
        Long p35 = createProduct(store3Id, catBeverages, "冷萃咖啡液 便携装 30条", "10小时低温萃取，即溶 / 10h cold extraction, instant dissolve", "35", 89.00, 250, 0);
        Long p36 = createProduct(store3Id, catSnacks, "山楂条 童年味道 500g", "承德铁山楂，酸甜软糯 / Chengde hawthorn, sweet-sour soft texture", "36", 19.90, 800, 451);

        // ========== Store 4 - 温馨家居 / Cozy Home ==========
        Long p37 = createProduct(store4Id, catFurniture, "北欧简约实木餐桌 1.4m", "白橡木材质，环保清漆 / White oak, eco-friendly varnish", "37", 1899.00, 20, 0);
        Long p38 = createProduct(store4Id, catFurniture, "人体工学办公椅", "4D扶手，135°后仰 / 4D armrests, 135° recline", "38", 999.00, 30, 0);
        Long p39 = createProduct(store4Id, catKitchenware, "德国不锈钢炒锅 32cm", "316L医用不锈钢，无涂层 / 316L surgical stainless steel, no coating", "39", 299.00, 100, 0);
        Long p40 = createProduct(store4Id, catKitchenware, "陶瓷刀具套装 5件套", "氧化锆陶瓷刀，永不生锈 / Zirconia ceramic blades, never rust", "40", 199.00, 150, 0);
        Long p41 = createProduct(store4Id, catStorage, "透明收纳箱 大号 60L×3", "加厚PP材质，可叠放 / Thick PP material, stackable", "41", 89.00, 300, 0);
        Long p42 = createProduct(store4Id, catStorage, "可折叠衣物收纳袋 6件套", "牛津布材质，防潮防尘 / Oxford fabric, moisture & dust proof", "42", 49.00, 400, 120);
        Long p43 = createProduct(store4Id, catFurniture, "简约布艺沙发 三人位", "高回弹海绵，可拆洗 / High-resilience foam, removable cover", "43", 2599.00, 10, 0);
        Long p44 = createProduct(store4Id, catFurniture, "记忆棉床垫 1.8m", "慢回弹记忆棉，分区支撑 / Slow-rebound memory foam, zoned support", "44", 1499.00, 15, 0);
        Long p45 = createProduct(store4Id, catKitchenware, "珐琅铸铁炖锅 24cm", "法国工艺，锁水循环 / French craftsmanship, moisture-lock design", "45", 399.00, 60, 0);
        Long p46 = createProduct(store4Id, catStorage, "双层鞋架 可调节 10层", "铁艺烤漆，节省空间 / Iron with baked finish, space-saving", "46", 69.00, 200, 81);
        Long p47 = createProduct(store4Id, catFurniture, "实木书架 落地置物架", "松木材质，多层收纳 / Pine wood, multi-tier storage", "47", 599.00, 25, 0);
        Long p48 = createProduct(store4Id, catKitchenware, "日式餐具套装 16件", "釉下彩工艺，微波炉可用 / Underglaze color, microwave safe", "48", 159.00, 120, 0);

        // ========== Store 5 - 活力运动馆 / Vitality Sports ==========
        Long p49 = createProduct(store5Id, catSportswear, "男士速干运动T恤", "吸湿排汗，抗菌除臭 / Moisture-wicking, antibacterial odor control", "49", 129.00, 300, 0);
        Long p50 = createProduct(store5Id, catSportswear, "女士瑜伽服套装", "四面弹力面料，裸感亲肤 / 4-way stretch fabric, bare-skin feel", "50", 199.00, 150, 0);
        Long p51 = createProduct(store5Id, catFitness, "可调节哑铃 20kg×2", "快速换重，节省空间 / Quick weight change, space efficient", "51", 399.00, 40, 0);
        Long p52 = createProduct(store5Id, catFitness, "专业瑜伽垫 加厚防滑", "10mm加厚TPE，双面防滑 / 10mm thick TPE, double-sided non-slip", "52", 89.00, 200, 0);
        Long p53 = createProduct(store5Id, catOutdoor, "户外登山背包 40L", "防水面料，背负系统 / Waterproof fabric, suspension system", "53", 299.00, 60, 0);
        Long p54 = createProduct(store5Id, catOutdoor, "双人露营帐篷 防风防雨", "3秒速开，UV50+防晒 / 3-second setup, UV50+ protection", "54", 499.00, 25, 0);
        Long p55 = createProduct(store5Id, catSportswear, "男士跑步鞋 轻量透气", "飞织鞋面，回弹中底 / Flyknit upper, responsive midsole", "55", 359.00, 100, 0);
        Long p56 = createProduct(store5Id, catFitness, "弹力带套装 5条装", "不同阻力级别，便携收纳 / Multiple resistance levels, portable", "56", 59.00, 350, 176);
        Long p57 = createProduct(store5Id, catOutdoor, "不锈钢保温杯 1L", "316不锈钢，24小时保温 / 316 stainless steel, 24h heat retention", "57", 79.00, 400, 0);
        Long p58 = createProduct(store5Id, catFitness, "运动护膝 专业支撑", "弹簧支撑，硅胶防滑 / Spring support, silicone anti-slip", "58", 99.00, 180, 0);
        Long p59 = createProduct(store5Id, catSportswear, "防晒皮肤衣 男女同款", "UPF50+，轻薄透气 / UPF50+, ultra-light breathable", "59", 169.00, 220, 0);
        Long p60 = createProduct(store5Id, catOutdoor, "折叠露营椅 便携式", "铝合金骨架，承重120kg / Aluminum frame, 120kg capacity", "60", 149.00, 80, 0);

        log.info("  商品创建完毕 / Products created: 60 total (12 per store)");
        log.info("===== 种子数据初始化完成 / Seed data initialization complete =====");
    }

    // ==================== 私有辅助方法 / Private helper methods ====================

    /**
     * 创建一个用户 / Create a single user
     *
     * @param username 用户名 / username
     * @param password 原始密码（将通过 BCrypt 哈希） / raw password (will be BCrypt-hashed)
     * @param nickname 昵称 / nickname
     * @param phone    手机号 / phone number
     * @param email    邮箱 / email
     * @param role     角色（0=BUYER, 1=SELLER, 2=ADMIN） / role
     * @param balance  账户余额 / account balance
     * @return 用户ID / the generated user ID
     */
    private Long createUser(String username, String password, String nickname,
                            String phone, String email, int role, BigDecimal balance) {
        FmUser user = new FmUser();
        user.setUsername(username);
        user.setPassword(PasswordUtil.hash(password));
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole(role);
        user.setBalance(balance);
        user.setStatus(1); // 正常 / active
        userMapper.insert(user);
        return user.getId();
    }

    /**
     * 创建一个商品分类 / Create a single product category
     *
     * @param parentId  父级分类ID（0表示顶级分类） / parent category ID (0 = top-level)
     * @param name      分类名称 / category name
     * @param sortOrder 排序序号 / sort order
     * @return 分类ID / the generated category ID
     */
    private Long createCategory(Long parentId, String name, int sortOrder) {
        FmProductCategory category = new FmProductCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setSortOrder(sortOrder);
        category.setStatus(1); // 正常 / active
        categoryMapper.insert(category);
        return category.getId();
    }

    /**
     * 创建一个店铺 / Create a single store
     *
     * @param userId      店主用户ID / store owner user ID
     * @param storeName   店铺名称 / store name
     * @param description 店铺描述 / store description
     * @return 店铺ID / the generated store ID
     */
    private Long createStore(Long userId, String storeName, String description) {
        FmStore store = new FmStore();
        store.setUserId(userId);
        store.setStoreName(storeName);
        store.setDescription(description);
        store.setStatus(StoreStatusEnum.APPROVED.getCode()); // 已审核通过 / approved
        storeMapper.insert(store);
        return store.getId();
    }

    /**
     * 创建一个商品及其主图 / Create a single product with its main image
     *
     * @param storeId    所属店铺ID / store ID
     * @param categoryId 所属分类ID / category ID
     * @param name       商品名称 / product name
     * @param description 商品描述 / product description
     * @param imageSuffix 图片编号后缀（如"01"） / image suffix number (e.g. "01")
     * @param price      价格 / price
     * @param stock      库存数量 / stock quantity
     * @param salesCount 已售数量 / units sold
     * @return 商品ID / the generated product ID
     */
    private Long createProduct(Long storeId, Long categoryId, String name,
                               String description, String imageSuffix,
                               double price, int stock, int salesCount) {
        // 创建商品 / Create product
        FmProduct product = new FmProduct();
        product.setStoreId(storeId);
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setDescription(description);
        product.setMainImage("/sql/image/product-" + imageSuffix + ".jpg");
        product.setPrice(BigDecimal.valueOf(price));
        product.setStock(stock);
        product.setStatus(ProductStatusEnum.APPROVED.getCode()); // 已审核通过 / approved
        product.setSalesCount(salesCount);
        productMapper.insert(product);

        // 创建商品主图 / Create product main image
        FmProductImage image = new FmProductImage();
        image.setProductId(product.getId());
        image.setImageUrl("/sql/image/product-" + imageSuffix + ".jpg");
        image.setSortOrder(0);
        productImageMapper.insert(image);

        return product.getId();
    }
}
