package com.lemondead1.carshopservice.repo;

import com.lemondead1.carshopservice.dto.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.ForeignKeyException;
import com.lemondead1.carshopservice.exceptions.RowNotFoundException;
import com.lemondead1.carshopservice.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.Csv;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;

import java.io.StringReader;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserRepoTest {
  private CarRepo cars;
  private UserRepo users;
  private OrderRepo orders;

  @BeforeEach
  void setup() {
    cars = new CarRepo();
    users = new UserRepo();
    orders = new OrderRepo();
    cars.setOrders(orders);
    users.setOrders(orders);
    orders.setCars(cars);
    orders.setUsers(users);
  }

  @Test
  void firstCreatedUserHasIdEqualToOne() {
    assertThat(users.create("username", "password", UserRole.CLIENT).id()).isEqualTo(1);
    assertThat(users.findById(1).id()).isEqualTo(1);
  }

  @Test
  void createdUserMatchesSpec() {
    users.create("user", "password", UserRole.CLIENT);
    users.create("admin", "password", UserRole.ADMIN);
    assertThat(users.findById(2)).isEqualTo(new User(2, "admin", "password", UserRole.ADMIN));
  }

  @Test
  void creatingUsersWithTheSameUsernameThrows() {
    users.create("user", "password", UserRole.CLIENT);
    assertThatThrownBy(() -> users.create("user", "password", UserRole.CLIENT))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void editedUserMatchesSpec() {
    users.create("user", "password", UserRole.CLIENT);
    users.edit(1).password("newPassword").role(UserRole.ADMIN).apply();
    assertThat(users.findById(1)).isEqualTo(new User(1, "user", "newPassword", UserRole.ADMIN));
  }

  @Test
  void editNotExistingUserThrows() {
    var builder = users.edit(1).username("username").password("password").role(UserRole.ADMIN);
    assertThatThrownBy(builder::apply).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void usernameConflictOnEditThrows() {
    users.create("user_1", "password", UserRole.CLIENT);
    users.create("user_2", "password", UserRole.ADMIN);
    var builder = users.edit(1).username("user_1").password("password").role(UserRole.ADMIN);
    assertThatThrownBy(builder::apply).isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  void deleteReturnsOldUser() {
    users.create("user", "password", UserRole.CLIENT);
    assertThat(users.delete(1)).isEqualTo(new User(1, "user", "password", UserRole.CLIENT));
  }

  @Test
  void deleteThrowsOnAbsentId() {
    assertThatThrownBy(() -> users.delete(10)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByIdThrowsAfterDelete() {
    users.create("user", "password", UserRole.CLIENT);
    users.delete(1);
    assertThatThrownBy(() -> users.findById(1)).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameThrowsAfterDelete() {
    users.create("user", "password", UserRole.CLIENT);
    users.delete(1);
    assertThatThrownBy(() -> users.findByUsername("user")).isInstanceOf(RowNotFoundException.class);
  }

  @Test
  void findByUsernameReturnsUser() {
    users.create("user", "password", UserRole.CLIENT);
    assertThat(users.findByUsername("user")).isEqualTo(new User(1, "user", "password", UserRole.CLIENT));
  }

  @Test
  void findByIdReturnsUser() {
    users.create("user", "password", UserRole.CLIENT);
    assertThat(users.findById(1)).isEqualTo(new User(1, "user", "password", UserRole.CLIENT));
  }

  @Test
  void deletingUserWithExistingOrdersThrows() {
    cars.create("BMW", "X5", 2015, 3000000, "good");
    users.create("alex", "pwd", UserRole.CLIENT);
    orders.create(Instant.now(), OrderKind.PURCHASE, OrderState.NEW, 1, 1, "ASAP");
    assertThatThrownBy(() -> users.delete(1)).isInstanceOf(ForeignKeyException.class);
  }

  @Nested
  class UserLookupTest {
    @BeforeEach
    void setup() {
      var csv = """
          1,lquarry0,wH5{`P@cu$hb6i,ADMIN
          2,darmytage1,bS6*4Y9m*oB,ADMIN
          3,egarbott2,"dO8}""14r_2kNOQ5d",ADMIN
          4,fbrusle3,"pL4,9m4JKRlx@E",ADMIN
          5,mlimpricht4,jZ4\\k4tv`\\i<.,MANAGER
          6,icopins5,oD7)wPJD',ADMIN
          7,ljulyan6,sS4%b=p?I8,ADMIN
          8,bprangnell7,tB2@?g(eHq|}(KZ,MANAGER
          9,cleahair8,cJ5<M5</Ux,MANAGER
          10,dbradburn9,sF0}tQL>(zZPyZw,ADMIN
          11,sdunkertona,tA2%q>VZ,ADMIN
          12,amcpaikb,dD4+9<jOGAoqqB,CLIENT
          13,ucroucherc,oS3'`$\\q~c)U,CLIENT
          14,btremlettd,rP4&&kbzg?0Vjy\\,MANAGER
          15,mbenfelle,aB7/%9~v`/2n`Pg_,ADMIN
          16,vpoulsenf,hG6`%7vJR0dlp,MANAGER
          17,kcheeldg,xV2&KI4S,ADMIN
          18,nwenzelh,nF5%b9lmvGIAS,MANAGER
          19,aalderwicki,aZ6>27ZK4{oGZ5,MANAGER
          20,mdenisonj,iP1'EpCnZIIa,ADMIN
          21,yrafteryk,tF0)BPb_U,ADMIN
          22,wmarplel,"jL5/""zgZCX|C",MANAGER
          23,lbonem,tQ9'3|k04z,ADMIN
          24,korrn,"mJ7""g@VYPGXeuf",MANAGER
          25,mrosoneo,uS7=XB&{ODx8O{,MANAGER
          26,mjeffsp,"hN6""GhtP@",MANAGER
          27,anovkovicq,fN8`FDP%~,MANAGER
          28,rreisenr,sD7?xjFQ/psK,ADMIN
          29,ebaldellis,mL5*?hPkfw1(ey,ADMIN
          30,dtrunchiont,eV8\\vE%7,ADMIN
          31,agorryu,rL5%4BAj*9J6%~A3,ADMIN
          32,mwicklinv,wL9={a.2,ADMIN
          33,sshilletow,hE9{kNLN,CLIENT
          34,dflethamx,nG5|MM5rJ#nd,ADMIN
          35,fskoulingy,jA4(wm~wB3zg,MANAGER""";
      var settings = Csv.parseRfc4180();
      settings.getFormat().setLineSeparator("\n");
      for (var row : new CsvParser(settings).iterate(new StringReader(csv))) {
        users.create(row[1], row[2], UserRole.valueOf(row[3]));
      }
    }

    @ParameterizedTest
    @CsvSource({
        "'1', lquarry",
        "'5,29,32,35', li"
    })
    void filterTest(String ids, String username) {
      assertThat(users.lookup(username, Set.of(UserRole.values()), UserSorting.USERNAME_ASC))
          .isSortedAccordingTo(UserSorting.USERNAME_ASC.getSorter())
          .map(User::id).contains(Arrays.stream(ids.split(",")).map(Integer::parseInt).toArray(Integer[]::new));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3 })
    void sortingTest(int sorterId) {
      assertThat(users.lookup("", Set.of(UserRole.values()), UserSorting.values()[sorterId]))
          .isSortedAccordingTo(UserSorting.values()[sorterId].getSorter())
          .map(User::id).containsAll(IntStream.range(1, 36).boxed().toList());
    }
  }
}
