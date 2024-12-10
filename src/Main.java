import java.sql.*;
import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main{

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
                System.out.println("\n동아리 관리 프로그램");
                System.out.println("1. 동아리 회원 관리");
                System.out.println("2. 동아리 활동 관리");
                System.out.println("3. 동아리 회비 관리");
                System.out.println("4. 테이블 목록 보기");
                System.out.println("5. 종료");
                System.out.print("선택: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // 줄바꿈 문자 제거

                switch (choice) {
                    case 1:
                        manageMembers(connection, scanner);
                        break;
                    case 2:
                        manageActivities(connection, scanner);
                        break;
                    case 3:
                        manageFees(connection, scanner);
                        break;
                    case 4:
                        listTables(connection);
                        break;
                    case 5:
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

    private static void manageMembers(Connection connection, Scanner scanner) {
        System.out.println("\n1. 회원 추가\n2. 회원 수정\n3. 회원 삭제\n4. 회원 조회");
        System.out.print("선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                insertMember(connection, scanner);
                break;
            case 2:
                updateMember(connection, scanner);
                break;
            case 3:
                deleteMember(connection, scanner);
                break;
            case 4:
                searchMembers(connection, scanner);
                break;
            default:
                System.out.println("잘못된 선택입니다.");
        }
    }

    private static void insertMember(Connection connection, Scanner scanner) {
        System.out.print("회원 이름: ");
        String name = scanner.nextLine();
        System.out.print("학번: ");
        String studentId = scanner.nextLine();
        System.out.print("전화번호: ");
        String phone = scanner.nextLine();

        String sql = "INSERT INTO members (name, student_id, phone) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, studentId);
            pstmt.setString(3, phone);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("회원 추가 성공!");
            }
        } catch (SQLException e) {
            System.out.println("회원 추가 오류: " + e.getMessage());
        }
    }

    private static void updateMember(Connection connection, Scanner scanner) {
        System.out.print("수정할 회원 ID: ");
        int memberId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("새 이름: ");
        String newName = scanner.nextLine();

        String sql = "UPDATE members SET name = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setInt(2, memberId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("회원 정보 수정 성공!");
            }
        } catch (SQLException e) {
            System.out.println("회원 정보 수정 오류: " + e.getMessage());
        }
    }

    private static void deleteMember(Connection connection, Scanner scanner) {
        System.out.print("삭제할 회원 ID: ");
        int memberId = scanner.nextInt();

        String sql = "DELETE FROM members WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("회원 삭제 성공!");
            }
        } catch (SQLException e) {
            System.out.println("회원 삭제 오류: " + e.getMessage());
        }
    }

    private static void searchMembers(Connection connection, Scanner scanner) {
        System.out.print("검색할 이름: ");
        String name = scanner.nextLine();

        String sql = "SELECT * FROM members WHERE name LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n회원 검색 결과:");
            while (rs.next()) {
                System.out.printf("ID: %d, 이름: %s, 학번: %s, 전화번호: %s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("student_id"), rs.getString("phone"));
            }
        } catch (SQLException e) {
            System.out.println("회원 검색 오류: " + e.getMessage());
        }
    }

    private static void manageActivities(Connection connection, Scanner scanner) {

    }

    private static void insertActivity(Connection connection, Scanner scanner) {

    }

    private static void searchActivities(Connection connection, Scanner scanner) {

    }

    private static void manageFees(Connection connection, Scanner scanner) {

    }

    private static void insertFee(Connection connection, Scanner scanner) {

    }

    private static void searchFees(Connection connection, Scanner scanner) {

    }

    private static void listTables(Connection connection) {

    }
}
