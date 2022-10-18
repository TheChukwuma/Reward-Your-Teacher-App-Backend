package com.decagon.rewardyourteacherapi11bjavapodf2.serviceImpl;

import com.decagon.rewardyourteacherapi11bjavapodf2.dto.EmailDetails;
import com.decagon.rewardyourteacherapi11bjavapodf2.enums.NotificationType;
import com.decagon.rewardyourteacherapi11bjavapodf2.enums.Role;
import com.decagon.rewardyourteacherapi11bjavapodf2.enums.TransactionType;
import com.decagon.rewardyourteacherapi11bjavapodf2.exceptions.InsufficientAmountException;
import com.decagon.rewardyourteacherapi11bjavapodf2.exceptions.UserNotFoundException;
import com.decagon.rewardyourteacherapi11bjavapodf2.exceptions.WalletNotFoundException;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Notification;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Transaction;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.User;
import com.decagon.rewardyourteacherapi11bjavapodf2.model.Wallet;
import com.decagon.rewardyourteacherapi11bjavapodf2.repository.NotificationRepository;
import com.decagon.rewardyourteacherapi11bjavapodf2.repository.TransactionRepository;
import com.decagon.rewardyourteacherapi11bjavapodf2.repository.UserRepository;
import com.decagon.rewardyourteacherapi11bjavapodf2.repository.WalletRepository;
import com.decagon.rewardyourteacherapi11bjavapodf2.response.ApiResponse;
import com.decagon.rewardyourteacherapi11bjavapodf2.security.CustomUserDetails;
import com.decagon.rewardyourteacherapi11bjavapodf2.service.EmailService;
import com.decagon.rewardyourteacherapi11bjavapodf2.service.RewardService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    private final NotificationRepository notificationRepository;

    private final TransactionRepository transactionRepository;

    private final EmailService emailService;

    @Override
    public ApiResponse<String>rewardTeacher(CustomUserDetails currentUser, Long teacherId, BigDecimal amount) {
            User user = userRepository.findUserByEmail(currentUser.getUsername()).orElseThrow(()-> new UserNotFoundException(currentUser.getUsername() + " not found"));
            Wallet userWallet = walletRepository.findWalletByUser(user).orElseThrow(()-> new WalletNotFoundException("Wallet Not Found"));

            User teacher = userRepository.findByIdAndRole(teacherId, Role.TEACHER);
            Optional<Wallet> teacherWallet = Optional.ofNullable(walletRepository.findWalletByUser(teacher).orElseThrow(() -> new WalletNotFoundException("Wallet Not Found")));

            if(userWallet.getAmount().compareTo(amount) >= 0){
                userWallet.setAmount(userWallet.getAmount().subtract(amount));
                teacherWallet.get().setAmount(teacherWallet.get().getAmount().add(amount));
                walletRepository.save(userWallet);
                walletRepository.save(teacherWallet.get());
                String message = "You sent ₦" + amount + " to " +   teacher.getName();
                String message2 = user.getName() + " sent you " + amount;
                notificationRepository.save(new Notification(message, NotificationType.DEBIT, user));
                notificationRepository.save(new Notification(message2,NotificationType.CREDIT, teacher));
                EmailDetails userEmailDetailsStudent = new EmailDetails(currentUser.getUsername(), message + "\nKind regards, \n@Reward App", "Debit Alert");
                EmailDetails userEmailDetailsTeacher = new EmailDetails(teacher.getEmail(), message2 + "\nKind regards, \n@Reward App", "Credit Alert");
                emailService.sendSimpleMail(userEmailDetailsStudent);
                emailService.sendSimpleMail(userEmailDetailsTeacher);

                Transaction transaction = new Transaction();
                transaction.setDescription(message);
                transaction.setTransactionType(TransactionType.DEBIT);
                transaction.setAmount(amount);
                transaction.setUser(user);
                transactionRepository.save(transaction);

                return new ApiResponse<>("Transfer successful", LocalDateTime.now(), message);
            }
            else{
                throw new InsufficientAmountException("Insufficient Fund");
            }


        }
    }

