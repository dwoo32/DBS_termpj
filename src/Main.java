import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

    private static final String URL_TEMPLATE = "jdbc:mysql://192.168.69.3:4567/%s";
    private static String USER;
    private static String PASSWORD;

    public static void main(String[] args) {

        try (FileInputStream fis = new FileInputStream(".env")) {
            Properties props = new Properties();
            props.load(fis);
            USER = props.getProperty("DB_USER");
            PASSWORD = props.getProperty("DB_PASSWORD");
        } catch (IOException e) {
            System.out.println(".env 파일을 불러오는 중 오류 발생: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("접속할 데이터베이스 이름을 입력하세요: ");
        String databaseName = scanner.nextLine();
        String url = String.format(URL_TEMPLATE, databaseName);

        try (Connection connection = DriverManager.getConnection(url, USER, PASSWORD)) {
            boolean exit = false;

            while (!exit) {
                System.out.println("\nMySQL " + databaseName + " Database 관리 프로그램");
                System.out.println("1. 테이블 목록 보기");
                System.out.println("2. 테이블 선택 후 데이터 삽입");
                System.out.println("3. 테이블 선택 후 데이터 삭제");
                System.out.println("4. 테이블 선택 후 데이터 검색");
                System.out.println("5. 테이블 구조 보기 (DESCRIBE)");
                System.out.println("6. 종료");
                System.out.print("선택: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // 줄바꿈 문자 제거

                switch (choice) {
                    case 1:
                        listTables(connection);
                        break;
                    case 2:
                        System.out.print("작업할 테이블 이름을 입력하세요: ");
                        String tableName = scanner.nextLine();
                        insertData(connection, scanner, tableName);
                        break;
                    case 3:
                        System.out.print("작업할 테이블 이름을 입력하세요: ");
                        String deleteTableName = scanner.nextLine();
                        deleteData(connection, scanner, deleteTableName);
                        break;
                    case 4:
                        System.out.print("작업할 테이블 이름을 입력하세요: ");
                        String searchTableName = scanner.nextLine();
                        searchData(connection, scanner, searchTableName);
                        break;
                    case 5:
                        System.out.print("구조를 볼 테이블 이름을 입력하세요: ");
                        String describeTableName = scanner.nextLine();
                        describeTable(connection, describeTableName);
                        break;
                    case 6:
                        exit = true;
                        break;
                    default:
                        System.out.println("잘못된 선택입니다. 다시 시도하세요.");
                }
            }
        } catch (SQLException e) {
            System.out.println("데이터베이스 연결 오류: " + e.getMessage());
        }
    }

    private static void listTables(Connection connection) {
        String sql = "SHOW TABLES";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n데이터베이스의 테이블 목록:");
            while (rs.next()) {
                String tableName = rs.getString(1);
                System.out.println(tableName);
            }
        } catch (SQLException e) {
            System.out.println("테이블 목록 조회 오류: " + e.getMessage());
        }
    }

    private static void insertData(Connection connection, Scanner scanner, String tableName) {
        String describeSql = "DESCRIBE " + tableName;
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(describeSql)) {
            System.out.println("\n데이터 삽입을 위한 테이블 " + tableName + "의 구조:");
            while (rs.next()) {
                String field = rs.getString("Field");
                String key = rs.getString("Key");
                String extra = rs.getString("Extra");

                // 자동 증가(AUTO_INCREMENT) 필드는 건너뜀
                if (key.equalsIgnoreCase("PRI") && extra.contains("auto_increment")) {
                    continue;
                }

                columns.add(field);
                System.out.print(field + " 값을 입력하세요: ");
                String value = scanner.nextLine();
                values.add(value);
            }
        } catch (SQLException e) {
            System.out.println("테이블 구조 조회 오류: " + e.getMessage());
            return;
        }


        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
        for (int i = 0; i < columns.size(); i++) {
            sqlBuilder.append(columns.get(i));
            if (i < columns.size() - 1) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            sqlBuilder.append("?");
            if (i < columns.size() - 1) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(")");

        String sql = sqlBuilder.toString();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setString(i + 1, values.get(i));
            }
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("데이터 삽입 성공!");
            }
        } catch (SQLException e) {
            System.out.println("데이터 삽입 오류: " + e.getMessage());
        }
    }

    private static void deleteData(Connection connection, Scanner scanner, String tableName) {
        System.out.print("삭제할 고객 ID: ");
        int id = scanner.nextInt();

        String sql = "DELETE FROM " + tableName + " WHERE custid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("데이터 삭제 성공!");
            } else {
                System.out.println("해당 ID를 가진 고객이 없습니다.");
            }
        } catch (SQLException e) {
            System.out.println("데이터 삭제 오류: " + e.getMessage());
        }
    }

    private static void searchData(Connection connection, Scanner scanner, String tableName) {
        System.out.print("검색할 고객 이름: ");
        String name = scanner.nextLine();

        String sql = "SELECT * FROM " + tableName + " WHERE name LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n검색 결과:");
            while (rs.next()) {
                int id = rs.getInt("custid");
                String customerName = rs.getString("name");
                String address = rs.getString("address");
                String phone = rs.getString("phone");
                System.out.printf("ID: %d, 이름: %s, 주소: %s, 전화번호: %s\n", id, customerName, address, phone);
            }
        } catch (SQLException e) {
            System.out.println("데이터 검색 오류: " + e.getMessage());
        }
    }

    private static void describeTable(Connection connection, String tableName) {
        String sql = "DESCRIBE " + tableName;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n테이블 " + tableName + "의 구조:");
            while (rs.next()) {
                String field = rs.getString("Field");
                String type = rs.getString("Type");
                String isNull = rs.getString("Null");
                String key = rs.getString("Key");
                String defaultValue = rs.getString("Default");
                String extra = rs.getString("Extra");
                System.out.printf("Field: %s, Type: %s, Null: %s, Key: %s, Default: %s, Extra: %s\n", field, type, isNull, key, defaultValue, extra);
            }
        } catch (SQLException e) {
            System.out.println("테이블 구조 조회 오류: " + e.getMessage());
        }
    }
}

