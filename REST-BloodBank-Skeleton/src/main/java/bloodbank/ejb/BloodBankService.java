/**
 * File: RecordService.java
 * Course materials (21W) CST 8277
 *
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * update by : I. Am. A. Student 040nnnnnnn
 *
 */
package bloodbank.ejb;


import static bloodbank.entity.Person.ALL_PERSONS_QUERY_NAME;
import static bloodbank.entity.Person.ALL_RECORDS_QUERY_NAME;
import static bloodbank.entity.BloodBank.ALL_BLOODBANKS_QUERY_NAME;
import static bloodbank.entity.BloodBank.ALL_BLOODDONATIONS_QUERY_NAME;
import static bloodbank.entity.BloodBank.FIND_BloodBank_BY_ID_QUERY;
import static bloodbank.entity.Person.FIND_PERSON_BY_ID_QUERY;
import static bloodbank.entity.SecurityRole.ROLE_BY_NAME_QUERY;
import static bloodbank.entity.SecurityUser.USER_FOR_OWNING_PERSON_QUERY;
import static bloodbank.utility.MyConstants.DEFAULT_KEY_SIZE;
import static bloodbank.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static bloodbank.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static bloodbank.utility.MyConstants.DEFAULT_SALT_SIZE;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PREFIX;
import static bloodbank.utility.MyConstants.PARAM1;
import static bloodbank.utility.MyConstants.PROPERTY_ALGORITHM;
import static bloodbank.utility.MyConstants.PROPERTY_ITERATIONS;
import static bloodbank.utility.MyConstants.PROPERTY_KEYSIZE;
import static bloodbank.utility.MyConstants.PROPERTY_SALTSIZE;
import static bloodbank.utility.MyConstants.PU_NAME;
import static bloodbank.utility.MyConstants.USER_ROLE;
import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.security.enterprise.identitystore.Pbkdf2PasswordHash;
import javax.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;

import bloodbank.entity.Address;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.Contact;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.entity.Phone;
import bloodbank.entity.SecurityRole;
import bloodbank.entity.SecurityUser;


/**
 * Stateless Singleton ejb Bean - BloodBankService
 */
@Singleton
public class BloodBankService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Person> getAllPeople() {
        return em.createNamedQuery(ALL_PERSONS_QUERY_NAME, Person.class).getResultList();
    }

    public Person getPersonId(int id) {
        Person bob = null;
        try {
            bob = em.createNamedQuery(FIND_PERSON_BY_ID_QUERY, Person.class).setParameter(id, PARAM1).getSingleResult();
            return bob;
        }
        catch (Exception e) {return null; }
    }
    

    @Transactional
    public Person persistPerson(Person newPerson) {
        em.persist(newPerson);
        return newPerson;
    }

    @Transactional
    public void buildUserForNewPerson(Person newPerson) {
        SecurityUser userForNewPerson = new SecurityUser();
        userForNewPerson.setUsername(
            DEFAULT_USER_PREFIX + "_" + newPerson.getFirstName() + "." + newPerson.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALTSIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEYSIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewPerson.setPwHash(pwHash);
        userForNewPerson.setPerson(newPerson);
        SecurityRole userRole = em.createNamedQuery(ROLE_BY_NAME_QUERY, SecurityRole.class)
            .setParameter(PARAM1, USER_ROLE).getSingleResult();
        userForNewPerson.getRoles().add(userRole);
        userRole.getUsers().add(userForNewPerson);
        em.persist(userForNewPerson);
    }

    @Transactional
    public Person setAddressFor(int id, Address newAddress) {
        Person addressPerson = em.find(Person.class, id); 
        newAddress.setContacts(addressPerson.getContacts());
        Set< Contact> addressContact = addressPerson.getContacts();
        
        ((Contact) addressContact).setAddress(newAddress);
        addressPerson.setContacts(addressContact);
        em.merge(addressPerson);
        em.flush();
        return addressPerson;
    }
    
    @Transactional
    public Person setPhoneFor(int id, Phone newPhone) {
        Person phonePerson = em.find(Person.class, id);
        newPhone.setContacts(phonePerson.getContacts());
        Set< Contact> addressContact = phonePerson.getContacts();
        ((Contact) addressContact).setPhone(newPhone);
        phonePerson.setContacts(addressContact);
        em.merge(phonePerson);
        em.flush();
        return phonePerson;
    }

    /**
     * to update a person
     * 
     * @param id - id of entity to update
     * @param personWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Person updatePersonById(int id, Person personWithUpdates) {
        Person personToBeUpdated = getPersonId(id);
        if (personToBeUpdated != null) {
            em.refresh(personToBeUpdated);
            em.merge(personWithUpdates);
            em.flush();
        }
        return personToBeUpdated;
    }

    /**
     * to delete a person by id
     * 
     * @param id - person id to delete
     */
    
    @Transactional
    public void deletePersonById(int id) {
        Person person = getPersonId(id);
        if (person != null) {
            em.refresh(person);
            TypedQuery<SecurityUser> findUser = em
                .createNamedQuery(USER_FOR_OWNING_PERSON_QUERY, SecurityUser.class)
                .setParameter(PARAM1, person.getId());
            SecurityUser sUser = findUser.getSingleResult();
            em.remove(sUser);
            em.remove(person);
        }
    }

    public List<BloodBank> getAllBloodBanks() {
        return em.createNamedQuery(ALL_BLOODBANKS_QUERY_NAME, BloodBank.class).getResultList();
    }

    public BloodBank getBloodBankById(int id) {
        BloodBank bb = null;
        try {bb = em.createNamedQuery(FIND_BloodBank_BY_ID_QUERY, BloodBank.class).setParameter(id, PARAM1).getSingleResult();
            return bb;
        }
        catch (Exception e) {return null; }
    }

    public boolean isDuplicated(BloodBank newBloodbank) {
        boolean placeholder = false;
        return placeholder;
    }

    @Transactional
    public BloodBank persistBloodBank(BloodBank newBloodbank) {
        em.persist(newBloodbank);
        return newBloodbank;
    }

    public BloodBank updateBloodBank(int bbID, BloodBank bb) {
        BloodBank bloodBankToBeUpdated = getBloodBankById(bbID);
        if (bloodBankToBeUpdated != null) {
            em.refresh(bloodBankToBeUpdated);
            em.merge(bloodBankToBeUpdated);
            em.flush();
        }
        return bloodBankToBeUpdated;
    }
    
    @Transactional
    public BloodBank deleteBloodBank(int id) {
        BloodBank bb = getBloodBankById(id);
        if (bb != null) {
            em.refresh(bb);  
            em.remove(bb);
        }
        return null;
    }
    
    @Transactional
    public BloodBank deleteDonationRecord(int id) {
        BloodBank bb = getBloodBankById(id);
        if (bb != null) {
            em.refresh(bb);  
            em.remove(bb);
        }
        return null;
    }
    
    public <T> T getById(Class<T> entity, String namedQuery, int id) {
        TypedQuery<T> searchQuery = em.createNamedQuery(namedQuery, entity);
        searchQuery.setParameter(PARAM1, id);
        return searchQuery.getSingleResult();
    }

    public List<BloodDonation> getAllDonations(BloodBank bb) {
        return em.createNamedQuery(ALL_BLOODDONATIONS_QUERY_NAME, BloodDonation.class).getResultList();
    }

    public List<DonationRecord> getAllRecords(Person pp) {
        return em.createNamedQuery(ALL_RECORDS_QUERY_NAME, DonationRecord.class).getResultList();
    }

    public Address getAddress(Person addressPerson) {
        // TODO Auto-generated method stub
        return null;
    }

    public Phone getPhone(Person phonePerson) {
        // TODO Auto-generated method stub
        return null;
    }
}
