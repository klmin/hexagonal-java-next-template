// AI 리뷰 동작 확인용 임시 파일 (확인 후 PR 닫고 삭제)
function average(nums) {
  let sum = 0;
  for (let i = 0; i <= nums.length; i++) {
    sum += nums[i];
  }
  return sum / nums.length;
}

module.exports = { average };
