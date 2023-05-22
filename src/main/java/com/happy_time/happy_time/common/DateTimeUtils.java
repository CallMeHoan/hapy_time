package com.happy_time.happy_time.common;

import com.happy_time.happy_time.constant.DateTimeConstant;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateTimeUtils {
    public static final String DEFAULT_FORMAT = " HH:mm:ss dd/MM/yyyy";
    public static final String BIRTHDAY_FORMAT_VI = "dd/MM/yyyy";
    public static final String BIRTHDAY_FORMAT_OTHER = "MM/dd/yyyy";

    public static final String DATE = "dd/MM/yyyy";

    public static Date parseFromString(String dateInString, String parsePatterns) {
        try {
            return DateUtils.parseDate(dateInString, DEFAULT_FORMAT, parsePatterns);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date getDateWithoutTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static Date addMonth(Date date, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, number);
        return cal.getTime();
    }

    public static Date addDate(Date date, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, number);
        return cal.getTime();
    }

    public static Date addMinute(Date date, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, number);
        return cal.getTime();
    }

    public static Date addHour(Date date, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, number);
        return cal.getTime();
    }

    public static Date addSencond(Date date, int number) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, number);
        return cal.getTime();
    }

    public static Integer getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }

    public static Integer getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }


    public static Integer getHour(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static Integer getMinute(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

    public static Integer getSecond(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.SECOND);
    }

    public static Integer getDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DATE);
    }

    public static Integer getDayOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static Long parseLongFromString(String dateString, String pattern) {
        Long result = null;
        try {
            Date date = new SimpleDateFormat(pattern).parse(dateString);
            result = date.getTime();
        } catch (Exception e) {
        }
        return result;
    }

    public static Long getTimeMillis(Integer value, String unit) {
        long result = 0;
        try {
            result = value * DateTimeConstant.MILLISECOND_MAP.get(unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Long getTimeSecond(Integer value, String unit) {
        long result = 0;
        try {
            result = value * DateTimeConstant.SECOND_MAP.get(unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static long getTimeMillis(Long value, String unit) {
        if (value == null) {
            return 0;
        }
        long result = 0;
        try {
            result = value * DateTimeConstant.MILLISECOND_MAP.get(unit);
        } catch (Exception e) {
        }
        return result;
    }

    public static Long getStartByCurrentDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime().getTime();
    }

    public static Long getStartOfAnyDay(long time_in_day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time_in_day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);

        return cal.getTime().getTime();
    }

    public static Long getStartOfAnyMonth(long time_in_day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time_in_day);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime().getTime();
    }

    public static Long getEndOfAnyDay(long time_in_day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time_in_day);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime().getTime();
    }


    public static Long getStartByDayOffset(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() - offset * 24 * 60 * 60 * 1000L);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 1);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime().getTime();
    }

    public static Long getEndByDayOffset(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() - offset * 24 * 60 * 60 * 1000L);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime().getTime();
    }

    // tra ve date dang string format dd_MM_yyyy
    public static String getDDMMYYYYAsString() {
        String result = "";
        try {
            long curent = System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(curent);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            return day + "_" + (month + 1) + "_" + year;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // tra ve ngày trong tuần dạng số theo thứ tự, bắt đầu từ chủ nhật
    public static List<Integer> getListDayOfWeekInIntegerType(List<String> day_of_week) {
        try {
            List<Integer> list = new ArrayList<>();
            if (!CollectionUtils.isEmpty(day_of_week)) {
                for (String day : day_of_week) {
                    switch (day) {
                        case "sunday":
                            list.add(1);
                            break;
                        case "monday":
                            list.add(2);
                            break;
                        case "tuesday":
                            list.add(3);
                            break;
                        case "wednesday":
                            list.add(4);
                            break;
                        case "thursday":
                            list.add(5);
                            break;
                        case "friday":
                            list.add(6);
                            break;
                        case "saturday":
                            list.add(7);
                            break;
                    }
                }
            }
            Collections.sort(list);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static long parseStartDayFromPattern(String str_date, String pattern) {
        long result = 0;
        try {
            Date date = new SimpleDateFormat(pattern).parse(str_date);
            result = date.getTime();
        } catch (Exception e) {
        }
        return result;
    }

    public static long parseEndDayFromPattern(String str_date, String pattern) {
        long result = 0;
        try {
            Date date = new SimpleDateFormat(pattern).parse(str_date);
            long time = date.getTime();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);

            result = cal.getTime().getTime();
        } catch (Exception e) {
        }
        return result;
    }

    public static String convertLongToDate(String pattern, Long time) {
        String result = null;
        try {
            Date date = new Date(time);
            result = new SimpleDateFormat(pattern).format(date);
        } catch (Exception e) {
        }
        return result;
    }

    // HH:mm dd/MM/yyyy
    public static Boolean checkValidDateTime(String date_string) {
        try {
            if (StringUtils.isBlank(date_string)) {
                return false;
            }

            List<String> split = Arrays.asList(date_string.split(" "));
            if (split.size() != 2) {
                return false;
            }

            return (checkValidTime(split.get(0)) && checkValidDate(split.get(1)));
        } catch (Exception e) {
            return false;
        }
    }

    // dd/MM/yyyy
    public static Boolean checkValidDate(String date_string) {
        try {
            if (StringUtils.isBlank(date_string)) {
                return false;
            }

            List<String> split = Arrays.asList(date_string.split("/"));
            if (split.size() != 3) {
                return false;
            }

            if (split.get(0).length() != 2 || StringUtils.trim(split.get(0)).length() != 2) {
                return false;
            }
            int day = Integer.parseInt(split.get(0));
            if (day < 0 || day > 31) {
                return false;
            }

            if (split.get(1).length() != 2 || StringUtils.trim(split.get(1)).length() != 2) {
                return false;
            }
            int month = Integer.parseInt(split.get(1));
            if (month < 0 || month > 12) {
                return false;
            }

            if (split.get(2).length() != 4 || StringUtils.trim(split.get(2)).length() != 4) {
                return false;
            }
            int year = Integer.parseInt(StringUtils.trim(split.get(2)));

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static Boolean checkValidTime(String date_string) {
        try {
            if (StringUtils.isBlank(date_string)) {
                return false;
            }

            List<String> split = Arrays.asList(date_string.split(":"));
            if (split.size() != 2) {
                return false;
            }

            if (split.get(0).length() != 2 || StringUtils.trim(split.get(0)).length() != 2) {
                return false;
            }
            int hour = Integer.parseInt(split.get(0));
            if (hour < 0 || hour > 23) {
                return false;
            }

            if (split.get(1).length() != 2 || StringUtils.trim(split.get(1)).length() != 2) {
                return false;
            }
            int minute = Integer.parseInt(split.get(1));
            if (minute < 0 || minute > 59) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String getDayOfWeekWithLanguage(Integer day, String language) {
        Map<Integer, String> day_of_week_en = new HashMap<>();
        day_of_week_en.put(1, "Sunday");
        day_of_week_en.put(2, "Monday");
        day_of_week_en.put(3, "Tuesday");
        day_of_week_en.put(4, "Wednesday");
        day_of_week_en.put(5, "Thursday");
        day_of_week_en.put(6, "Friday");
        day_of_week_en.put(7, "Saturday");

        Map<Integer, String> day_of_week_vi = new HashMap<>();
        day_of_week_vi.put(1, "Chủ nhật");
        day_of_week_vi.put(2, "Thứ hai");
        day_of_week_vi.put(3, "Thứ ba");
        day_of_week_vi.put(4, "Thứ tư");
        day_of_week_vi.put(5, "Thứ năm");
        day_of_week_vi.put(6, "Thứ sáu");
        day_of_week_vi.put(7, "Thứ bảy");

        Map<Integer, String> day_of_week_kh = new HashMap<>();
        day_of_week_kh.put(1, "ថ្ងៃអាទិត្យ");
        day_of_week_kh.put(2, "ទីពីរ");
        day_of_week_kh.put(3, "ថ្ងៃអង្គារ");
        day_of_week_kh.put(4, "ថ្ងៃពុធ");
        day_of_week_kh.put(5, "ថ្ងៃព្រហស្បតិ៍");
        day_of_week_kh.put(6, "ថ្ងៃសុក្រ");
        day_of_week_kh.put(7, "ថ្ងៃសៅរ៍");
        String res = "";
        switch (language) {
            case "vi":
                res = day_of_week_vi.get(day);
                break;
            case "kh":
                res = day_of_week_kh.get(day);
                break;
            case "en":
                res = day_of_week_en.get(day);
                break;
        }
        return res;
    }


    public static int getDayOfWeek(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int result = calendar.get(Calendar.DAY_OF_WEEK);
        if(result == 1) {
            result = 8;
        }
        return result;
    }

    public static String getNextDay(String current_day) {
        try {
            int day = Integer.parseInt(current_day);
            if (day < 2 || day >= 7) {
                return null;
            }
            return String.valueOf(day + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Integer getNextDay(int current_day) {
        if (current_day < 2 || current_day > 8) {
            return null;
        }
        if(current_day == 8) {
            return 2;
        }
        return current_day + 1;

    }
}
