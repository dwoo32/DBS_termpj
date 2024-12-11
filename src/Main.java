import java.sql.*;
import java.util.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ClubManagement {

    private static final String URL_TEMPLATE = "jdbc:mysql://192.168.69.3:4567/Club";
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
                System.out.println("4. 동아리 공지 관리");
                System.out.println("5. 테이블 목록 보기");
                System.out.println("6. 종료");
                System.out.print("선택: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // 줄바꿈 문자 제거

                // 종료 선택 시 역할 확인 없이 루프 종료
                if (choice == 6) {
                    exit = true;
                    break;
                }

                // 사용자 역할 확인
                System.out.print("사용자 역할 (임원/Member): ");
                String role = scanner.nextLine();

                if (!role.equals("임원") && !role.equals("Member")) {
                    System.out.println("잘못된 역할입니다. 프로그램을 종료합니다.");
                    break;
                }

                switch (choice) {
                    case 1:
                        if (role.equals("임원")) {
                            manageMembers(connection, scanner);
                        } else {
                            System.out.println("권한이 없습니다.");
                        }
                        break;
                    case 2:
                        if (role.equals("임원")) {
                            manageActivities(connection, scanner);
                        } else {
                            System.out.println("권한이 없습니다.");
                        }
                        break;
                    case 3:
                        manageFees(connection, scanner);
                        break;
                    case 4:
                        if (role.equals("임원")) {
                            manageNotices(connection, scanner);
                        } else {
                            System.out.println("권한이 없습니다.");
                        }
                        break;
                    case 5:
                        listTables(connection);
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

    private static void manageActivities(Connection connection, Scanner scanner) {
        System.out.println("\n1. 활동 추가\n2. 활동 수정\n3. 활동 삭제\n4. 활동 조회");
        System.out.print("선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                insertActivity(connection, scanner);
                break;
            case 2:
                updateActivity(connection, scanner);
                break;
            case 3:
                deleteActivity(connection, scanner);
                break;
            case 4:
                searchActivities(connection, scanner);
                break;
            default:
                System.out.println("잘못된 선택입니다.");
        }
    }

    private static void manageFees(Connection connection, Scanner scanner) {
        System.out.println("\n1. 회비 추가\n2. 회비 삭제\n3. 회비 조회");
        System.out.print("선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                insertFee(connection, scanner);
                break;
            case 2:
                deleteFee(connection, scanner);
                break;
            case 3:
                searchFees(connection, scanner);
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
        System.out.print("역할 (임원/Member): ");
        String role = scanner.nextLine();

        String sql = "INSERT INTO members (name, student_id, phone, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, studentId);
            pstmt.setString(3, phone);
            pstmt.setString(4, role);
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
        System.out.print("새 역할 (임원/Member): ");
        String newRole = scanner.nextLine();

        String sql = "UPDATE members SET name = ?, role = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newRole);
            pstmt.setInt(3, memberId);
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
            } else {
                System.out.println("해당 회원을 찾을 수 없습니다.");
            }
        } catch (SQLException e) {
            System.out.println("회원 삭제 오류: " + e.getMessage());
        }
    }

    private static void searchMembers(Connection connection, Scanner scanner) {
        System.out.print("검색할 회원 이름 키워드: ");
        String keyword = scanner.nextLine();

        String sql = "SELECT * FROM members WHERE name LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n회원 검색 결과:");
            while (rs.next()) {
                System.out.printf("ID: %d, 이름: %s, 학번: %s, 역할: %s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("student_id"), rs.getString("role"));
            }
        } catch (SQLException e) {
            System.out.println("회원 검색 오류: " + e.getMessage());
        }
    }

    private static void insertActivity(Connection connection, Scanner scanner) {
        System.out.print("활동 이름: ");
        String name = scanner.nextLine();
        System.out.print("활동 날짜 (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("활동 설명: ");
        String description = scanner.nextLine();
        System.out.print("동아리 ID: ");
        int clubId = scanner.nextInt();

        String sql = "INSERT INTO activities (name, date, description, club_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, date);
            pstmt.setString(3, description);
            pstmt.setInt(4, clubId);
            pstmt.executeUpdate();
            System.out.println("활동 추가 성공!");
        } catch (SQLException e) {
            System.out.println("활동 추가 오류: " + e.getMessage());
        }
    }

    private static void updateActivity(Connection connection, Scanner scanner) {
        System.out.print("수정할 활동 ID: ");
        int activityId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("새 활동 이름: ");
        String newName = scanner.nextLine();
        System.out.print("새 활동 설명: ");
        String newDescription = scanner.nextLine();

        String sql = "UPDATE activities SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newDescription);
            pstmt.setInt(3, activityId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("활동 수정 성공!");
            }
        } catch (SQLException e) {
            System.out.println("활동 수정 오류: " + e.getMessage());
        }
    }

    private static void deleteActivity(Connection connection, Scanner scanner) {
        System.out.print("삭제할 활동 ID: ");
        int activityId = scanner.nextInt();

        String sql = "DELETE FROM activities WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("활동 삭제 성공!");
            }
        } catch (SQLException e) {
            System.out.println("활동 삭제 오류: " + e.getMessage());
        }
    }

    private static void searchActivities(Connection connection, Scanner scanner) {
        System.out.print("검색할 활동 이름 키워드: ");
        String keyword = scanner.nextLine();

        String sql = "SELECT * FROM activities WHERE name LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n활동 검색 결과:");
            while (rs.next()) {
                System.out.printf("ID: %d, 이름: %s, 날짜: %s, 설명: %s\n",
                        rs.getInt("id"), rs.getString("name"), rs.getString("date"), rs.getString("description"));
            }
        } catch (SQLException e) {
            System.out.println("활동 검색 오류: " + e.getMessage());
        }
    }

    private static void insertFee(Connection connection, Scanner scanner) {
        System.out.print("회비 금액: ");
        double amount = scanner.nextDouble();
        System.out.print("회원 ID: ");
        int memberId = scanner.nextInt();
        System.out.print("동아리 ID: ");
        int clubId = scanner.nextInt();

        String sql = "INSERT INTO fees (amount, member_id, club_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, memberId);
            pstmt.setInt(3, clubId);
            pstmt.executeUpdate();
            System.out.println("회비 추가 성공!");
        } catch (SQLException e) {
            System.out.println("회비 추가 오류: " + e.getMessage());
        }
    }

    private static void deleteFee(Connection connection, Scanner scanner) {
        System.out.print("삭제할 회비 ID: ");
        int feeId = scanner.nextInt();

        String sql = "DELETE FROM fees WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, feeId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("회비 삭제 성공!");
            }
        } catch (SQLException e) {
            System.out.println("회비 삭제 오류: " + e.getMessage());
        }
    }

    private static void searchFees(Connection connection, Scanner scanner) {
        System.out.print("검색할 회원 ID: ");
        int memberId = scanner.nextInt();

        String sql = "SELECT * FROM fees WHERE member_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n회비 검색 결과:");
            while (rs.next()) {
                System.out.printf("ID: %d, 금액: %.2f, 날짜: %s, 동아리 ID: %d\n",
                        rs.getInt("id"), rs.getDouble("amount"), rs.getString("payment_date"), rs.getInt("club_id"));
            }
        } catch (SQLException e) {
            System.out.println("회비 검색 오류: " + e.getMessage());
        }
    }

    private static void manageNotices(Connection connection, Scanner scanner) {
        System.out.println("\n1. 공지 추가\n2. 공지 수정\n3. 공지 삭제\n4. 공지 조회");
        System.out.print("선택: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                insertNotice(connection, scanner);
                break;
            case 2:
                updateNotice(connection, scanner);
                break;
            case 3:
                deleteNotice(connection, scanner);
                break;
            case 4:
                searchNotices(connection, scanner);
                break;
            default:
                System.out.println("잘못된 선택입니다.");
        }
    }

    private static void insertNotice(Connection connection, Scanner scanner) {
        System.out.print("공지 제목: ");
        String title = scanner.nextLine();
        System.out.print("공지 내용: ");
        String content = scanner.nextLine();
        System.out.print("동아리 ID: ");
        int clubId = scanner.nextInt();

        String sql = "INSERT INTO notices (title, content, date_posted, club_id) VALUES (?, ?, NOW(), ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setInt(3, clubId);
            pstmt.executeUpdate();
            System.out.println("공지 추가 성공!");
        } catch (SQLException e) {
            System.out.println("공지 추가 오류: " + e.getMessage());
        }
    }

    private static void updateNotice(Connection connection, Scanner scanner) {
        System.out.print("수정할 공지 ID: ");
        int noticeId = scanner.nextInt();
        scanner.nextLine();
        System.out.print("새 제목: ");
        String newTitle = scanner.nextLine();
        System.out.print("새 내용: ");
        String newContent = scanner.nextLine();

        String sql = "UPDATE notices SET title = ?, content = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newContent);
            pstmt.setInt(3, noticeId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("공지 수정 성공!");
            }
        } catch (SQLException e) {
            System.out.println("공지 수정 오류: " + e.getMessage());
        }
    }

    private static void deleteNotice(Connection connection, Scanner scanner) {
        System.out.print("삭제할 공지 ID: ");
        int noticeId = scanner.nextInt();

        String sql = "DELETE FROM notices WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, noticeId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("공지 삭제 성공!");
            }
        } catch (SQLException e) {
            System.out.println("공지 삭제 오류: " + e.getMessage());
        }
    }

    private static void searchNotices(Connection connection, Scanner scanner) {
        System.out.print("검색할 공지 제목 키워드: ");
        String keyword = scanner.nextLine();

        String sql = "SELECT * FROM notices WHERE title LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n공지 검색 결과:");
            while (rs.next()) {
                System.out.printf("ID: %d, 제목: %s, 내용: %s, 게시일: %s\n",
                        rs.getInt("id"), rs.getString("title"), rs.getString("content"), rs.getString("date_posted"));
            }
        } catch (SQLException e) {
            System.out.println("공지 검색 오류: " + e.getMessage());
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
}
