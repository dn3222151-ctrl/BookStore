package com.example.bookstore.Service;

import com.example.bookstore.Entity.OrderDetails;
import com.example.bookstore.Entity.Orders;
import com.example.bookstore.Entity.User;

import java.util.Arrays;
import java.util.List;

public class CultivationHelper {

    public static final String[] REALMS = {
            "Phàm Nhân", "Luyện Khí Kỳ", "Trúc Cơ Kỳ", "Kim Đan Kỳ",
            "Nguyên Anh Kỳ", "Hóa Thần Kỳ", "Luyện Hư Kỳ", "Đại Thừa Kỳ", "Độ Kiếp Phi Thăng"
    };

    // Danh hiệu tương ứng với từng cảnh giới
    public static final String[] TITLES = {
            "Sơ Nhập Thư Các",
            "Học Đồ Độc Giả",
            "Thanh Vân Thư Sinh",
            "Kim Đan Học Sĩ",
            "Nguyên Anh Tông Sư",
            "Hóa Thần Đại Năng",
            "Luyện Hư Đạo Tôn",
            "Đại Thừa Thánh Giả",
            "Thiên Đế Chí Tôn"
    };

    private static final long[] THRESHOLDS = { 0, 20, 60, 150, 300, 500, 800, 1200, 2000 };

    private static final String[] BADGES = {
            "bg-secondary",
            "bg-info text-dark",
            "bg-primary",
            "bg-warning text-dark",
            "bg-success",
            "bg-danger",
            "realm-badge-6", // Tím huyền bí (nổi bật trên cả nền sáng & tối)
            "realm-badge-7", // Cam Hoàng Kim
            "realm-badge-8"  // Đế Vương Bạch Kim - Hoàng Kim
    };

    private static final String[] NAME_CLASSES = {
            "realm-name-0", "realm-name-1", "realm-name-2", "realm-name-3",
            "realm-name-4", "realm-name-5", "realm-name-6", "realm-name-7", "realm-name-8"
    };

    private static final String[] FRAME_CLASSES = {
            "realm-frame-0", "realm-frame-1", "realm-frame-2", "realm-frame-3",
            "realm-frame-4", "realm-frame-5", "realm-frame-6", "realm-frame-7", "realm-frame-8"
    };

    private static final int[] DISCOUNTS = { 0, 2, 5, 8, 12, 15, 18, 20, 25 };

    private static final String[] PERKS = {
            "Thành viên cơ bản",
            "Giảm 2% đơn hàng • Huy hiệu trên Diễn đàn",
            "Giảm 5% đơn hàng • Ưu tiên xử lý đơn",
            "Giảm 8% đơn hàng • Miễn phí vận chuyển",
            "Giảm 12% đơn hàng • Miễn phí ship • Khung bài nổi bật trên Diễn đàn",
            "Giảm 15% đơn hàng • Miễn phí ship • Khung bài nổi bật trên Diễn đàn",
            "Giảm 18% đơn hàng • Miễn phí ship • Khung bài VIP trên Diễn đàn",
            "Giảm 20% đơn hàng • Miễn phí ship • Khung bài VIP trên Diễn đàn",
            "Giảm 25% đơn hàng • Miễn phí ship • Danh hiệu Thiên Đế Chí Tôn & Khung Hoàng Kim"
    };

    public static class RealmInfo {
        public final int level;
        public final String name;
        public final String title;
        public final String badgeClass;
        public final String nextRealmName;
        public final int progressPercent;
        public final String nextGoalText;
        public final int discountPercent;
        public final String perks;
        public final boolean highRealm;
        public final boolean customAssigned;
        public final String nameClass;
        public final String frameClass;

        public RealmInfo(int level, String name, String title, String badgeClass, String nextRealmName,
                         int progressPercent, String nextGoalText, int discountPercent, String perks,
                         boolean highRealm, boolean customAssigned, String nameClass, String frameClass) {
            this.level = level;
            this.name = name;
            this.title = title;
            this.badgeClass = badgeClass;
            this.nextRealmName = nextRealmName;
            this.progressPercent = progressPercent;
            this.nextGoalText = nextGoalText;
            this.discountPercent = discountPercent;
            this.perks = perks;
            this.highRealm = highRealm;
            this.customAssigned = customAssigned;
            this.nameClass = nameClass;
            this.frameClass = frameClass;
        }
    }

    public static List<String> getAllRealms() {
        return Arrays.asList(REALMS);
    }

    public static RealmInfo calculateRealm(double totalSpent, int totalBooksBought) {
        return calculateRealm(totalSpent, totalBooksBought, null);
    }

    public static RealmInfo calculateRealm(double totalSpent, int totalBooksBought, String customRealm) {
        if (customRealm != null && !customRealm.trim().isEmpty()) {
            for (int i = 0; i < REALMS.length; i++) {
                if (REALMS[i].equalsIgnoreCase(customRealm.trim())) {
                    return new RealmInfo(
                            i, REALMS[i], TITLES[i], BADGES[i],
                            i == REALMS.length - 1 ? "Cảnh giới tối cao" : REALMS[i + 1],
                            100,
                            "Cảnh giới được Quản trị viên ban thưởng",
                            DISCOUNTS[i], PERKS[i], i >= 4, true,
                            NAME_CLASSES[i], FRAME_CLASSES[i]
                    );
                }
            }
        }

        long points = Math.round(totalSpent / 10000.0) + (totalBooksBought * 5L);
        int idx = 0;
        for (int i = REALMS.length - 1; i >= 0; i--) {
            if (points >= THRESHOLDS[i]) {
                idx = i;
                break;
            }
        }

        if (idx == REALMS.length - 1) {
            return new RealmInfo(
                    idx, REALMS[idx], TITLES[idx], BADGES[idx], "Đã đạt cảnh giới tối cao", 100,
                    "Tối đa", DISCOUNTS[idx], PERKS[idx], true, false,
                    NAME_CLASSES[idx], FRAME_CLASSES[idx]
            );
        }

        long currentMin = THRESHOLDS[idx];
        long nextMin = THRESHOLDS[idx + 1];
        int progress = (int) Math.min(100, Math.max(0, ((points - currentMin) * 100) / (nextMin - currentMin)));
        long needed = nextMin - points;

        return new RealmInfo(
                idx, REALMS[idx], TITLES[idx], BADGES[idx], REALMS[idx + 1], progress,
                "Cần thêm " + needed + " điểm tu vi để đột phá " + REALMS[idx + 1],
                DISCOUNTS[idx], PERKS[idx], idx >= 4, false,
                NAME_CLASSES[idx], FRAME_CLASSES[idx]
        );
    }

    public static RealmInfo calculateForUser(User user, List<Orders> orders) {
        if (user == null) {
            return calculateRealm(0, 0, null);
        }
        double totalSpent = 0.0;
        int totalBooks = 0;
        if (orders != null) {
            for (Orders o : orders) {
                if ("CANCELLED".equalsIgnoreCase(o.getStatus()) || "Đã hủy".equalsIgnoreCase(o.getStatus())) {
                    continue;
                }
                totalSpent += o.getTotalAmount();
                if (o.getOrderDetails() != null) {
                    for (OrderDetails d : o.getOrderDetails()) {
                        totalBooks += d.getQuantity();
                    }
                }
            }
        }
        return calculateRealm(totalSpent, totalBooks, user.getCustomRealm());
    }
}