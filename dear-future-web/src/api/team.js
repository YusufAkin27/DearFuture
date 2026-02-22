import api from './axiosConfig';

/**
 * Backend üzerinden GitHub kullanıcı bilgisi ve repoları (README ile) getirir.
 * Token sadece backend'de kullanılır.
 */
export async function getGitHubUser(username) {
    const response = await api.get(`/public/team/github/${encodeURIComponent(username)}`);
    return response.data;
}
