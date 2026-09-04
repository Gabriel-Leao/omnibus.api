package br.com.leao.gabriel.omnibus.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StaffTest {

  @Test
  void shouldCreateActiveStaffMember() {
    var staff =
        Staff.createByAdmin(
            "João", "joao@example.com", "hash", StaffRole.EDITOR, "EMP-001", StaffDepartment.IT);

    assertThat(staff.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(staff.getAccountType()).isEqualTo(AccountType.STAFF);
    assertThat(staff.getRole()).isEqualTo(StaffRole.EDITOR);
  }
}
