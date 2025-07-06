contract TestContract {
    event TestEvent(uint256 indexed value);

    function testLoop() public {
        for (uint256 i = 1; i <= 10; i++) {
            emit TestEvent(i);
        }
    }
}