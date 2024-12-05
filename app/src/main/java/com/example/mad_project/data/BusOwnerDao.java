package com.example.mad_project.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BusOwnerDao {
    @Insert
    long insert(BusOwner busOwner);

    @Update
    void update(BusOwner busOwner);

    @Query("SELECT * FROM bus_owners WHERE user_id = :userId AND is_active = 1")
    BusOwner getBusOwnerByUserId(int userId);

    @Query("SELECT * FROM bus_owners WHERE company_registration = :regNumber AND is_active = 1")
    BusOwner getBusOwnerByRegistration(String regNumber);

    @Query("UPDATE bus_owners SET is_active = 0 WHERE id = :ownerId")
    void deactivateOwner(int ownerId);

    @Query("SELECT EXISTS(SELECT 1 FROM bus_owners WHERE company_registration = :regNumber)")
    boolean isCompanyRegistrationExists(String regNumber);

    @Query("SELECT EXISTS(SELECT 1 FROM bus_owners WHERE tax_id = :taxId)")
    boolean isTaxIdExists(String taxId);

    @Query("SELECT * FROM bus_owners WHERE id = :ownerId")
    BusOwner getBusOwnerById(int ownerId);
}
