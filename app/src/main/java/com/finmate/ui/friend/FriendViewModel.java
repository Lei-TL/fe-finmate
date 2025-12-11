package com.finmate.ui.friend;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.finmate.data.dto.FriendResponse;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.data.repository.FriendRemoteRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class FriendViewModel extends ViewModel {

    private final FriendRemoteRepository friendRemoteRepository;

    private final MutableLiveData<List<FriendUIModel>> _friends = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<FriendUIModel>> friends = _friends;

    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> loading = _loading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    @Inject
    public FriendViewModel(FriendRemoteRepository friendRemoteRepository) {
        this.friendRemoteRepository = friendRemoteRepository;
    }

    public void loadFriends() {
        _loading.postValue(true);

        friendRemoteRepository.getFriends(new ApiCallback<List<FriendResponse>>() {
            @Override
            public void onSuccess(List<FriendResponse> body) {
                List<FriendUIModel> ui = new ArrayList<>();
                if (body != null) {
                    for (FriendResponse dto : body) {
                        ui.add(new FriendUIModel(
                                dto.getId(),
                                dto.getFriendUserId(),
                                dto.getFriendName(),
                                dto.getFriendEmail(),
                                dto.getStatus(),
                                dto.isIncoming()
                        ));
                    }
                }
                _friends.postValue(ui);
                _loading.postValue(false);
            }

            @Override
            public void onError(String message, Integer code) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }

    public void sendRequest(String email) {
        _loading.postValue(true);
        friendRemoteRepository.sendRequest(email, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void body) {
                // gửi xong, reload list
                loadFriends();
            }

            @Override
            public void onError(String message, Integer code) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }

    public void accept(String friendshipId) {
        _loading.postValue(true);
        friendRemoteRepository.accept(friendshipId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void body) {
                loadFriends();
            }

            @Override
            public void onError(String message, Integer code) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }

    public void reject(String friendshipId) {
        _loading.postValue(true);
        friendRemoteRepository.reject(friendshipId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void body) {
                loadFriends();
            }

            @Override
            public void onError(String message, Integer code) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }

    public void remove(String friendshipId) {
        _loading.postValue(true);
        friendRemoteRepository.remove(friendshipId, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void body) {
                loadFriends();
            }

            @Override
            public void onError(String message, Integer code) {
                _error.postValue(message);
                _loading.postValue(false);
            }
        });
    }
}
