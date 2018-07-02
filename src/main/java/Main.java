import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.System.err;
import static java.lang.System.out;
import static java.lang.Thread.sleep;

// тестирование jdbcPostgres

public class Main {

    static Map<String, Long> hmCookieTime =
            Collections.synchronizedMap(new HashMap<String, Long>());
    static jdbcPostgres psql = new jdbcPostgres();

    static {
        //jdbc кеширование в HM кук(Str) и времени(Str) БД, таблицы cooks
        try {
            ResultSet rsRowCount = psql.execute("SELECT count(*) FROM cooks"); // количество записей
            rsRowCount.next();
            int rowsCount = Integer.parseInt(rsRowCount.getString(1));
            // out.println("rowsCount: " + rowsCount);

            ResultSet rsData = psql.execute("SELECT * FROM cooks");
            for (int c = 1; c <= rowsCount; c++) {
                rsData.next();
                String cook = rsData.getString(1);
                long time = Long.parseLong(rsData.getString(2));
                // out.println(cook + ": " + time);
                hmCookieTime.put(cook, time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        } finally {
//            psql.closeConnection();
//        }
    }


    public static void main(String[] args) {
        try {
            out.println("\n 1. PGSQL testing Select queryes");
            ResultSet rsForCC = psql.execute("select * from clients"); // для получения количеста колонок
            out.println("Column count = " + rsForCC.getMetaData().getColumnCount());

            ResultSet rsForRC = psql.execute("select count(*) from clients"); // для получения количеста записей
            rsForRC.next();
            out.println("Row count = " + rsForRC.getString(1));

            ResultSet rs = psql.execute("SELECT * from clients where login='first'");
            out.println("In search by login \"first\" result rows: " + psql.getRowCount(rs));


//            out.println("\n 2. Cook and HashMap tests");
//            long timeNow = System.currentTimeMillis();
//            Cookie coo = new Cookie("cook", String.valueOf(timeNow));
//            out.println("View: " + coo.getName() + ":" + coo.getValue());
//            out.println("Пут новая пара");
//            hmCookieTime.put(coo.getName(), timeNow);
//            out.println("Вот она: " + coo.getName() + ":" + hmCookieTime.get(coo.getName()));
//            out.println("Пробуем добавить по тому же ключу");
//            hmCookieTime.put(coo.getValue(), (timeNow + 1));
//            out.println("Добавили");
//            out.println("Теперь она: " + coo.getValue() + ":" + hmCookieTime.get(coo.getValue()));
//            out.println("Ищем в HashMap несущ-й ключ: " + hmCookieTime.get("not real key"));
//            out.println("HashMap all keys: " + hmCookieTime.keySet());

            //jdbc
//            ResultSet rsRowCount = psql.execute("SELECT count(*) FROM cooks"); // количество записей
//            rsRowCount.next();
//            int rowsCount = Integer.parseInt(rsRowCount.getString(1));
//            out.println("rowsCount: "+rowsCount);
//
//            ResultSet rsData = psql.execute("SELECT * FROM cooks");
//            for (int c = 1; c<=rowsCount;c++){
//                rsData.next();
//                String cook = rsData.getString(1);
//                long time = Long.parseLong(rsData.getString(2));
//                out.println(cook+": "+time);
//                hmCookieTime.put(cook, time);
//            }
            out.println("DONE");
            out.println(hmCookieTime.keySet());

            /*
            in DB:
                cOokIeTeSt: 1527441055061
                cooTest1: 1527493625487
                cooTest2: 1527493775487
            hm.keySet():
                [cook, 1529232657189, cOokIeTeSt, cooTest2, cooTest1]
             */


            MyThread mt10 = new MyThread(0, 1000);
//            MyThread mtInfinity = new MyThread(1000);
            new Thread(mt10).start();
//            new Thread(mtInfinity).start();

            //ждем 10 секунд и проверяем счетчики по первому и второму потоку
//            Thread.sleep(10000);
//            out.println("Counters:\nmt10: "+mt10.getCounter()+"\nmtInfinity: "+mtInfinity.getCounter());
//            Thread.sleep(20000);
//            out.println("Counters2:\nmt10: "+mt10.getCounter()+"\nmtInfinity: "+mtInfinity.getCounter());

            long from = getTimeNow();
            out.println(from);
            Thread.sleep(500);
            long to = getTimeNow();
            out.println(to);
            out.println("\nDIFF:\nsec: " + diffSeconds(from, to) + "\nmin: " + diffMinutes(from, to));
            out.println("test: " + diffMinutes(1, 68001));

//            //Тест удаления из HashMap
//            out.println("\n ____ Тест удаления из HashMap ____");
//            Map<String, String> hm = new HashMap<>();
//            hm.put("key", "val");
//            out.println("Remove present pair: " + hm.remove("key"));
//            out.println("Remove nothing: " + hm.remove("key"));

            //Тест удаления из PSQL
            out.println("\n ____ Тест удаления из PSQL ____");

            String QUERY_DEFAULT_DATA = "INSERT INTO cooks(cookie, time) VALUES " +
                    "('cook_test1', '1529658000001')," +
                    "('cook_test2', '1529658000002')," +
                    "('cook_test3', '1529658000003')";

            // TODO: готово
            // 01. Добавить запись, если уже есть - обновить
            // (Обновление времени куки, если клиент зашел)
            String QUERY_INSERT_ONE = "INSERT INTO cooks(cookie, time) VALUES ('cook_test1', '1529658000001')" +
                    " ON CONFLICT (cookie) DO UPDATE SET time =  '1529658000001'";
//            out.println(QUERY_INSERT_ONE + ":\n" + psql.execute(QUERY_INSERT_ONE));

            // 02. поиск (ХЗ)
            String QUERY_SELECT_ONE = "SELECT * FROM cooks WHERE cookie='cook_test'";
            ResultSet rsSelectOne = psql.execute(QUERY_SELECT_ONE);
//            out.println(QUERY_SELECT_ONE + ":\n" + psql.getRowCount(rsSelectOne));


            // 03. Удаление
            String QUERY_RM_ONE = "DELETE FROM cooks WHERE cookie='cook_test777'";
            psql.execute(QUERY_RM_ONE); // "Запрос не вернул результата"
            out.println("\n" + QUERY_RM_ONE + ":\n" + "");



            // Наполним тестовыми данными
//            hmCookieTime.put("cook1", getTimeNow()-60000); //текущее время минус 1 минуту
//            hmCookieTime.put("cook2", getTimeNow()-12*60000); //текущее время минус ~12 минут

            String[] arr = hmCookieTime.keySet().toArray(new String[0]);


            out.println();
            //Проверка и удаление элементов HM
            //timeNow уже выше определялось
            // До
            /**

            for (String s : arr){
                long timeSaveCook = hmCookieTime.get(s); //берем время куки(String) из HM >> в long
                long diff = timeNow-timeSaveCook;
                float diffSec = (float) (diff/1000);
                float diffMin = diffSec/60;
                boolean needRemove = (diffMin>10);
                out.println(s+":"+"\n"+"Diff is: "+diff+"(Sec: "+diffSec+", Min: "+diffMin+").\nNeed remove: "+needRemove+"\n");
                //Удаление
                if (needRemove) {
                    //из HM
                    hmCookieTime.remove(s);
                    //из PG
                    psql.execute("DELETE FROM cooks WHERE cookie='"+s+"'");
                }
            }

            // После
            out.println(hmCookieTime.keySet());
             */
            Logger logger = Logger.getLogger( Main.class.getName() );
            FileHandler fh = new FileHandler("loggerExample.log", false);
            fh.setLevel(Level.FINE);

            Logger l = Logger.getLogger("");
            fh.setFormatter(new SimpleFormatter());
            l.addHandler(fh);
            l.setLevel(Level.CONFIG);
            logger.log(Level.INFO, "message 1");
            logger.log(Level.SEVERE, "message 2");
            logger.log(Level.FINE, "message 3");

//            LOGGER.addHandler(fh);
//            LOGGER.log( Level.FINE, "processing entries in loop");
//            LOGGER.log( Level.FINE, "processing entries in loop", 14);


//            psql.execute3("DELETE FROM cooks WHERE cookie='$0'", new String[]{"cook2"}); // Не пашет :(

            out.println("\n-- Thread example --");

            TenMinutesThread tmt = new TenMinutesThread();
            Thread thr = new Thread(tmt);
//            thr.start();

            out.println("\n-- File write example (for Logs) --");
            String textForWrite = "192.168.0.1\n" + "192.168.0.5\n";
            try {
                Files.write(Paths.get("//home//nnm//test.log"), textForWrite.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            File file = new File("//home//nnm//.android");
            out.println(file.exists());
            out.println(Arrays.asList(new File("//home//nnm//").list()).contains(".android"));
//            out.println(Arrays.toString(str[5]));

            out.println("KS: "+ Cookies.hmCookieTime.keySet());

            ResultSet rsUser = psql.execute("SELECT * FROM clients WHERE login='user' AND password='password'");
            rsUser.next();
            out.println(rsUser.getString(3)); // Если пользователь не найден, тут случается ошибка "ResultSet..perhap"







        } catch (Exception e) {
            err.println(e.getMessage());
            e.printStackTrace();
        }
//        finally {
//            psql.closeConnection();
//        }
    }



    static class EncDec{

    }

    // Класс-поток, можно указать интервал и число запусков
    static class MyThread implements Runnable {
        long counterGlobal = 0; //общий счетчик итераций
        int limit;
        int sleepTime;
        boolean isInfinity = false;

        //бесконечное выполнение
        MyThread(int sleepTime) {
            this.sleepTime = sleepTime;
            this.isInfinity = true;
        }

        //выполняется 'limit'-раз
        MyThread(int limit, int sleepTime) {
            this.limit = limit;
            this.sleepTime = sleepTime;
        }

        public long getCounter() {
            return counterGlobal;
        }

        public void run() {
            if (isInfinity) {
                while (true) {
                    out.println(">> the infinity thread <<");
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counterGlobal++;
                }
            } else {
                int counter = 0;
                while (counter <= limit) {
                    out.println(">> " + counter + " <<");
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                }
                counterGlobal++;
            }

        }
    }

    private static float diffSeconds(long from, long to) {
        return (to - from) / 1000;
    }

    private static float diffMinutes(long from, long to) {
        return (to - from) / 60000;
    }

    private static long getTimeNow() {
        return System.currentTimeMillis();
    }

    static class TenMinutesThread implements Runnable {
        public void run() {
            try {
                while (true) {
                    err.println("3sec >>");
                    try {
                        sleep(3000); //60000*10 минут 10
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    err.println("3sec <<");

                    String[] arr = hmCookieTime.keySet().toArray(new String[0]); //arr become =["key-0","key-N"]
                    out.println(hmCookieTime.keySet());
                    for (String s : arr) {
                        long timeSaveCook = hmCookieTime.get(s); //берем время куки(long) из HM
                        float diffMinutes = (float) (getTimeNow() - timeSaveCook) / 60000;
                        //TODO: need realize writing to log
                        //Удаление если возраст куки более 10ти минут
                        if (diffMinutes > 10) {
                            out.println("REMOVING: " + s);
                            //из HM
                            hmCookieTime.remove(s);
                            //из PG
                            psql.execute("DELETE FROM cooks WHERE cookie='" + s + "'");
                        }
                    }
                    out.println("\n");
                    new Main();
                }
            }catch (Exception ignored){}
            finally {
                psql.closeConnection();
            }
        }
    }
}