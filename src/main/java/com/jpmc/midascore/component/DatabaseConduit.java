package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseConduit {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConduit.class);
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public DatabaseConduit(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }

    @Transactional
    public boolean processTransaction(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (!isValidTransaction(sender, recipient, transaction.getAmount())) {
            logger.warn("Invalid transaction: {}", transaction);
            return false;
        }

        // Update balances
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());

        // Save records
        userRepository.save(sender);
        userRepository.save(recipient);

        // Record transaction
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount());
        transactionRepository.save(transactionRecord);

        return true;
    }

    // Validate transaction for valid sender, recipient, and amount
    private boolean isValidTransaction(UserRecord sender, UserRecord recipient, float amount) {

        // Is null ok here? In real scenario may need more logic to determine if user
        // and
        // recipient are valid and calling that function
        return sender != null &&
                recipient != null &&
                sender.getBalance() >= amount;
    }

    // Find user by name to find Waldorf
    public UserRecord findUserByName(String name) {
        for (UserRecord user : userRepository.findAll()) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }
}
